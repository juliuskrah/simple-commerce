package com.simplecommerce.discount;

import java.math.BigDecimal;

/**
 * Service for discount management and validation.
 *
 * @author julius.krah
 */
public interface DiscountService {

  /**
   * Validate and apply a discount code to an order.
   *
   * @param code the discount code
   * @param customerId the customer ID
   * @param orderAmount the order subtotal
   * @return the discount result with amount and details
   */
  DiscountResult validateAndApplyDiscount(String code, String customerId, BigDecimal orderAmount);

  /**
   * Calculate the discount amount for a code without applying it.
   *
   * @param code the discount code
   * @param orderAmount the order amount
   * @return the discount amount
   */
  BigDecimal calculateDiscountAmount(String code, BigDecimal orderAmount);
}
