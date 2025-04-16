package com.simplecommerce.checkout;

/**
 * Enumeration of events related to carts.
 * @author julius.krah
 * @since 1.0
 */
public enum Carts {
  CART_CREATED,
  CART_UPDATED,
  CART_DELETED,
  ITEM_ADDED,
  ITEM_REMOVED,
  ITEM_UPDATED,
  CHECKOUT_STARTED,
  CHECKOUT_COMPLETED,
  CHECKOUT_FAILED,
  PAYMENT_STARTED,
  PAYMENT_COMPLETED,
  PAYMENT_FAILED,
  ORDER_CREATED,
  ORDER_UPDATED,
  ORDER_DELETED,
  ORDER_COMPLETED,
  ORDER_FAILED
}
