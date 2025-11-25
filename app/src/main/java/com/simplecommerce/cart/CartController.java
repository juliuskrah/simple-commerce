package com.simplecommerce.cart;

import static com.simplecommerce.shared.types.Types.NODE_CART;
import static com.simplecommerce.shared.types.Types.NODE_CART_ITEM;

import com.simplecommerce.actor.Actor;
import com.simplecommerce.product.variant.ProductVariant;
import com.simplecommerce.product.variant.ProductVariantService;
import com.simplecommerce.shared.GlobalId;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * Controller for shopping cart operations.
 *
 * @author julius.krah
 * @since 1.0
 */
@Controller
class CartController {

  private static final Logger LOG = LoggerFactory.getLogger(CartController.class);

  private final ObjectProvider<CartService> cartService;
  private final Supplier<CartService> cartServiceSupplier = SingletonSupplier.of(CartManagement::new);
  private final ObjectProvider<ProductVariantService> variantService;

  CartController(
      ObjectProvider<CartService> cartService,
      ObjectProvider<ProductVariantService> variantService) {
    this.cartService = cartService;
    this.variantService = variantService;
  }

  @QueryMapping
  Cart cart() {
    LOG.debug("Fetching current user's cart");
    return cartService.getIfAvailable(cartServiceSupplier).getOrCreateCart();
  }

  @QueryMapping
  Cart cartById(@Argument String id) {
    LOG.debug("Fetching cart by id: {}", id);
    return cartService.getIfAvailable(cartServiceSupplier).findCartById(id);
  }

  @SchemaMapping(typeName = "Cart")
  String id(Cart source) {
    return source.id();
  }

  @SchemaMapping(typeName = "Cart")
  Optional<Actor> customer(Cart source) {
    // Customer will be resolved by Actor controller if needed
    return Optional.empty();
  }

  @SchemaMapping(typeName = "CartItem")
  String id(CartItem source) {
    return source.id();
  }

  @SchemaMapping(typeName = "CartItem")
  ProductVariant variant(CartItem source) {
    // The variant is already resolved in the service layer
    return source.variant();
  }

  @MutationMapping
  Cart addToCart(@Argument AddToCartInput input) {
    LOG.debug("Adding item to cart: variantId={}, quantity={}", input.variantId(), input.quantity());
    return cartService.getIfAvailable(cartServiceSupplier).addToCart(input);
  }

  @MutationMapping
  Cart updateCartItem(@Argument UpdateCartItemInput input) {
    LOG.debug("Updating cart item: cartItemId={}, quantity={}", input.cartItemId(), input.quantity());
    return cartService.getIfAvailable(cartServiceSupplier).updateCartItem(input);
  }

  @MutationMapping
  Cart removeFromCart(@Argument String cartItemId) {
    LOG.debug("Removing item from cart: cartItemId={}", cartItemId);
    return cartService.getIfAvailable(cartServiceSupplier).removeFromCart(cartItemId);
  }

  @MutationMapping
  Cart clearCart() {
    LOG.debug("Clearing cart");
    return cartService.getIfAvailable(cartServiceSupplier).clearCart();
  }

  @MutationMapping
  Cart mergeCart(@Argument String guestCartId) {
    LOG.debug("Merging guest cart: guestCartId={}", guestCartId);
    return cartService.getIfAvailable(cartServiceSupplier).mergeCart(guestCartId);
  }
}
