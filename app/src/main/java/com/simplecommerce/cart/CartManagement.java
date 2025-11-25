package com.simplecommerce.cart;

import com.simplecommerce.actor.ActorEntity;
import com.simplecommerce.actor.Actors;
import com.simplecommerce.product.pricing.PriceResolutionService;
import com.simplecommerce.product.variant.ProductVariantManagement;
import com.simplecommerce.product.variant.ProductVariantService;
import com.simplecommerce.product.variant.ProductVariants;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.types.Money;
import com.simplecommerce.shared.utils.SecurityUtils;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import javax.money.MonetaryAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CartService for managing shopping carts.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class CartManagement implements CartService {

  private static final Logger LOG = LoggerFactory.getLogger(CartManagement.class);
  private static final int CART_EXPIRY_DAYS = 30;

  public void setCartRepository(ObjectFactory<Carts> cartRepository) {
    this.cartRepository = cartRepository.getObject();
  }

  public void setCartItemRepository(ObjectFactory<CartItems> cartItemRepository) {
    this.cartItemRepository = cartItemRepository.getObject();
  }

  public void setVariantRepository(ObjectFactory<ProductVariants> variantRepository) {
    this.variantRepository = variantRepository.getObject();
  }

  public void setPriceResolutionService(ObjectFactory<PriceResolutionService> priceResolutionService) {
    this.priceResolutionService = priceResolutionService.getObject();
  }

  public void setActorRepository(ObjectFactory<Actors> actorRepository) {
    this.actorRepository = actorRepository.getObject();
  }

  private Carts cartRepository;
  private CartItems cartItemRepository;
  private ProductVariants variantRepository;
  private ProductVariantService variantService = new ProductVariantManagement();
  private PriceResolutionService priceResolutionService;
  private Actors actorRepository;

  @Override
  @Transactional(readOnly = true)
  public Cart getOrCreateCart() {
    var currentUser = getCurrentUser();
    CartEntity cartEntity;

    if (currentUser != null) {
      // Authenticated user - find or create cart
      cartEntity = cartRepository.findByCustomerId(currentUser.getId())
          .orElseGet(() -> createCartForUser(currentUser.getId()));
    } else {
      throw new IllegalStateException("Cannot create cart without user or session ID");
    }

    return toCart(cartEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public Cart findCartById(String id) {
    var globalId = GlobalId.decode(id);
    var cartEntity = cartRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Cart not found"));
    return toCart(cartEntity);
  }

  @Override
  public Cart addToCart(AddToCartInput input) {
    LOG.debug("Adding item to cart: variantId={}, quantity={}", input.variantId(), input.quantity());

    // Get or create cart
    CartEntity cartEntity = getOrCreateCartEntity(input.sessionId());

    // Get variant
    var globalId = GlobalId.decode(input.variantId());
    var variant = variantRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Product variant not found"));

    // Resolve price for the variant
    var price = priceResolutionService.resolvePrice(variant, null, Locale.getDefault())
        .orElseThrow(() -> new IllegalStateException("No price found for variant"));

    // Create cart item
    var cartItem = new CartItemEntity();
    cartItem.setVariant(variant);
    cartItem.setQuantity(input.quantity());
    cartItem.setUnitPrice(convertMoneyToMonetaryAmount(price));

    // Add to cart
    cartEntity.addItem(cartItem);
    cartEntity = cartRepository.save(cartEntity);

    LOG.debug("Added item to cart: cartId={}, cartItemId={}", cartEntity.getId(), cartItem.getId());
    return toCart(cartEntity);
  }

  @Override
  public Cart updateCartItem(UpdateCartItemInput input) {
    LOG.debug("Updating cart item: cartItemId={}, quantity={}", input.cartItemId(), input.quantity());

    var globalId = GlobalId.decode(input.cartItemId());
    var cartItem = cartItemRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Cart item not found"));

    cartItem.setQuantity(input.quantity());
    cartItem = cartItemRepository.save(cartItem);

    var cart = cartItem.getCart();
    LOG.debug("Updated cart item: cartId={}, cartItemId={}", cart.getId(), cartItem.getId());
    return toCart(cart);
  }

  @Override
  public Cart removeFromCart(String cartItemId) {
    LOG.debug("Removing item from cart: cartItemId={}", cartItemId);

    var globalId = GlobalId.decode(cartItemId);
    var cartItem = cartItemRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Cart item not found"));

    var cart = cartItem.getCart();
    cart.removeItem(cartItem);
    cartItemRepository.delete(cartItem);

    LOG.debug("Removed item from cart: cartId={}, cartItemId={}", cart.getId(), globalId.id());
    return toCart(cart);
  }

  @Override
  public Cart clearCart() {
    LOG.debug("Clearing cart");

    var cartEntity = getOrCreateCartEntity(null);
    cartEntity.clearItems();
    cartEntity = cartRepository.save(cartEntity);

    LOG.debug("Cleared cart: cartId={}", cartEntity.getId());
    return toCart(cartEntity);
  }

  @Override
  public Cart mergeCart(String guestCartId) {
    LOG.debug("Merging guest cart: guestCartId={}", guestCartId);

    var currentUser = getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("User must be authenticated to merge carts");
    }

    var globalId = GlobalId.decode(guestCartId);
    var guestCart = cartRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Guest cart not found"));

    var userCart = cartRepository.findByCustomerId(currentUser.getId())
        .orElseGet(() -> createCartForUser(currentUser.getId()));

    // Merge items from guest cart to user cart
    for (var item : guestCart.getItems()) {
      userCart.addItem(item);
    }

    userCart = cartRepository.save(userCart);
    cartRepository.delete(guestCart);

    LOG.debug("Merged carts: guestCartId={}, userCartId={}", globalId.id(), userCart.getId());
    return toCart(userCart);
  }

  private CartEntity getOrCreateCartEntity(String sessionId) {
    var currentUser = getCurrentUser();

    if (currentUser != null) {
      return cartRepository.findByCustomerId(currentUser.getId())
          .orElseGet(() -> createCartForUser(currentUser.getId()));
    } else if (sessionId != null) {
      return cartRepository.findBySessionId(sessionId)
          .orElseGet(() -> createCartForSession(sessionId));
    } else {
      throw new IllegalStateException("Cannot create cart without user or session ID");
    }
  }

  private CartEntity createCartForUser(UUID userId) {
    var actor = actorRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    var cart = new CartEntity();
    cart.setCustomer(actor);
    cart.setExpiresAt(OffsetDateTime.now().plusDays(CART_EXPIRY_DAYS));
    return cartRepository.save(cart);
  }

  private CartEntity createCartForSession(String sessionId) {
    var cart = new CartEntity();
    cart.setSessionId(sessionId);
    cart.setExpiresAt(OffsetDateTime.now().plusDays(CART_EXPIRY_DAYS));
    return cartRepository.save(cart);
  }

  private ActorEntity getCurrentUser() {
    return SecurityUtils.getCurrentUserLogin()
        .flatMap(actorRepository::findByUsername)
        .orElse(null);
  }

  private Cart toCart(CartEntity entity) {
    var items = entity.getItems().stream()
        .map(this::toCartItem)
        .toList();

    var subtotal = calculateSubtotal(entity);
    var tax = calculateTax(subtotal);
    var total = subtotal.add(tax);

    return new Cart(
        new GlobalId("Cart", entity.getId().toString()).encode(),
        null, // customer will be resolved by GraphQL
        entity.getSessionId(),
        items,
        entity.getTotalQuantity(),
        convertMonetaryAmountToMoney(subtotal),
        convertMonetaryAmountToMoney(tax),
        convertMonetaryAmountToMoney(total),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getExpiresAt()
    );
  }

  private CartItem toCartItem(CartItemEntity entity) {
    var variant = variantService.findVariant(
        entity.getVariant().getId().toString()
    );

    var unitPrice = entity.getUnitPrice();
    var totalPrice = entity.getTotalPrice();

    return new CartItem(
        new GlobalId("CartItem", entity.getId().toString()).encode(),
        variant,
        entity.getQuantity(),
        convertMonetaryAmountToMoney(unitPrice),
        convertMonetaryAmountToMoney(totalPrice),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }

  private MonetaryAmount calculateSubtotal(CartEntity cart) {
    return cart.getItems().stream()
        .map(CartItemEntity::getTotalPrice)
        .reduce(MonetaryAmount::add)
        .orElse(createZeroAmount());
  }

  private MonetaryAmount calculateTax(MonetaryAmount subtotal) {
    // TODO: Implement proper tax calculation
    // For now, return 0
    return createZeroAmount();
  }

  private MonetaryAmount createZeroAmount() {
    return org.javamoney.moneta.Money.of(BigDecimal.ZERO, "USD");
  }

  private MonetaryAmount convertMoneyToMonetaryAmount(Money money) {
    return org.javamoney.moneta.Money.of(money.amount(), money.currency().toString());
  }

  private Money convertMonetaryAmountToMoney(MonetaryAmount amount) {
    var currencyUnit = amount.getCurrency();
    var amountValue = amount.getNumber().numberValue(BigDecimal.class);
    return new Money(currencyUnit, amountValue);
  }
}
