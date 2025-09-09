package com.simplecommerce.customer;

/**
 * Defines different customer groups for tiered pricing and permissions.
 *
 * @author julius.krah
 * @since 1.0
 */
public enum CustomerGroup {
  /**
   * Business to Consumer - Regular retail customers.
   */
  B2C,

  /**
   * Business to Business - Corporate customers with special pricing.
   */
  B2B,

  /**
   * VIP customers with premium benefits and pricing.
   */
  VIP,

  /**
   * Wholesale customers with bulk pricing discounts.
   */
  WHOLESALE
}