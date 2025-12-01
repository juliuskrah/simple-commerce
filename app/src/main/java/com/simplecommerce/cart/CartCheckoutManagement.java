package com.simplecommerce.cart;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CartCheckoutService for checkout operations.
 *
 * @author julius.krah
 */
@Service
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
class CartCheckoutManagement implements CartCheckoutService {

  private static final Logger LOG = LoggerFactory.getLogger(CartCheckoutManagement.class);

  private Carts cartRepository;

  public void setCartRepository(ObjectFactory<Carts> cartRepository) {
    this.cartRepository = cartRepository.getObject();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CartEntity> getCartEntityById(UUID cartId) {
    return cartRepository.findById(cartId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CartEntity> getCartEntityByCustomerId(UUID customerId) {
    return cartRepository.findByCustomerId(customerId);
  }

  @Override
  public void clearCartAfterCheckout(CartEntity cartEntity) {
    LOG.debug("Clearing cart after checkout: cartId={}", cartEntity.getId());
    cartEntity.clearItems();
    cartRepository.save(cartEntity);
  }
}
