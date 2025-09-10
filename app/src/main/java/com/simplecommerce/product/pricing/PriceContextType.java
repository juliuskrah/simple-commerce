package com.simplecommerce.product.pricing;

/**
 * Defines the different types of pricing contexts that can be applied to price rules.
 *
 * @author julius.krah
 * @since 1.0
 */
public enum PriceContextType {
  /**
   * Geographic context (country, region, state).
   * Context values: "US", "EU", "UK", "CA", etc.
   */
  GEOGRAPHIC,

  /**
   * Currency context for multi-currency pricing.
   * Context values: "USD", "EUR", "GBP", etc.
   */
  CURRENCY,

  /**
   * Customer group context for tiered pricing.
   * Context values: "B2B", "B2C", "VIP", "WHOLESALE", etc.
   */
  CUSTOMER_GROUP,

  /**
   * Default pricing rule (no specific context).
   * Used as fallback when no other rules match.
   */
  DEFAULT
}