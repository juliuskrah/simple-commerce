package com.simplecommerce.cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Internal API for cart operations during checkout.
 * This service is used by the order module to access cart data during checkout.
 *
 * @author julius.krah
 */
public interface CartCheckoutService {

  /**
   * Get a cart entity by ID for checkout processing.
   *
   * @param cartId the cart ID
   * @return the cart entity if found
   */
  Optional<CartEntity> getCartEntityById(UUID cartId);

  /**
   * Get a cart entity by customer ID for checkout processing.
   *
   * @param customerId the customer ID
   * @return the cart entity if found
   */
  Optional<CartEntity> getCartEntityByCustomerId(UUID customerId);

  /**
   * Clear all items from a cart after successful checkout.
   *
   * @param cartEntity the cart entity to clear
   */
  void clearCartAfterCheckout(CartEntity cartEntity);
}
