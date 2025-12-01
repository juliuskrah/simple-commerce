package com.simplecommerce.discount;

/**
 * Types of discounts.
 *
 * @author julius.krah
 */
public enum DiscountType {
  /** Percentage off order */
  PERCENTAGE,

  /** Fixed amount off order */
  FIXED_AMOUNT,

  /** Free shipping */
  FREE_SHIPPING,

  /** Buy X get Y free */
  BUY_X_GET_Y
}
