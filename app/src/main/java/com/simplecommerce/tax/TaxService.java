package com.simplecommerce.tax;

import java.math.BigDecimal;

/**
 * Service for tax calculations.
 *
 * @author julius.krah
 */
public interface TaxService {

  /**
   * Calculate tax for an order.
   *
   * @param subtotal the subtotal amount
   * @param currency the currency
   * @param country the destination country
   * @param state the destination state/region
   * @return the calculated tax amount
   */
  BigDecimal calculateTax(BigDecimal subtotal, String currency, String country, String state);

  /**
   * Get the tax rate for a location.
   *
   * @param country the country code
   * @param state the state/region code
   * @return the tax rate as a percentage
   */
  BigDecimal getTaxRate(String country, String state);
}
