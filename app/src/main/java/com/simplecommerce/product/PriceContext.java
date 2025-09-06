package com.simplecommerce.product;

/**
 * Represents the context in which a price is being resolved.
 * This includes all the dimensions that can affect pricing:
 * geographic location, currency, customer group, etc.
 *
 * @param customerGroup the customer group (B2B, B2C, VIP, etc.)
 * @param region the geographic region (US, EU, UK, etc.)
 * @param currency the currency code (USD, EUR, GBP, etc.)
 * @param quantity the quantity being purchased (for tiered pricing)
 *
 * @author julius.krah
 * @since 1.0
 */
public record PriceContext(
    String customerGroup,
    String region,
    String currency,
    Integer quantity
) {

  /**
   * Creates a default price context with minimal information.
   *
   * @param currency the currency code
   * @return a PriceContext with default values
   */
  public static PriceContext defaultContext(String currency) {
    return new PriceContext(null, null, currency, 1);
  }

  /**
   * Creates a price context for B2C customers.
   *
   * @param region the geographic region
   * @param currency the currency code
   * @param quantity the quantity
   * @return a PriceContext for B2C customers
   */
  public static PriceContext b2c(String region, String currency, Integer quantity) {
    return new PriceContext("B2C", region, currency, quantity);
  }

  /**
   * Creates a price context for B2B customers.
   *
   * @param region the geographic region
   * @param currency the currency code
   * @param quantity the quantity
   * @return a PriceContext for B2B customers
   */
  public static PriceContext b2b(String region, String currency, Integer quantity) {
    return new PriceContext("B2B", region, currency, quantity);
  }

  /**
   * Creates a new context with a different quantity.
   *
   * @param newQuantity the new quantity
   * @return a new PriceContext with updated quantity
   */
  public PriceContext withQuantity(Integer newQuantity) {
    return new PriceContext(customerGroup, region, currency, newQuantity);
  }

  /**
   * Creates a new context with a different customer group.
   *
   * @param newCustomerGroup the new customer group
   * @return a new PriceContext with updated customer group
   */
  public PriceContext withCustomerGroup(String newCustomerGroup) {
    return new PriceContext(newCustomerGroup, region, currency, quantity);
  }
}