package com.simplecommerce.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simple tax calculation service with hardcoded rates.
 * In production, this should integrate with tax calculation APIs like Avalara or TaxJar.
 *
 * @author julius.krah
 */
@Service
public class SimpleTaxService implements TaxService {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleTaxService.class);

  @Override
  public BigDecimal calculateTax(BigDecimal subtotal, String currency, String country, String state) {
    var taxRate = getTaxRate(country, state);
    var tax = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);

    LOG.debug("Calculated tax: subtotal={}, rate={}%, tax={}", subtotal, taxRate, tax);
    return tax;
  }

  @Override
  public BigDecimal getTaxRate(String country, String state) {
    // Simple hardcoded tax rates
    // TODO: Integrate with real tax calculation service
    return switch (country) {
      case "US" -> getUSTaxRate(state);
      case "GB" -> BigDecimal.valueOf(20L); // UK VAT
      case "DE" -> BigDecimal.valueOf(19L); // German VAT
      case "FR" -> BigDecimal.valueOf(20L); // French VAT
      default -> BigDecimal.ZERO;
    };
  }

  private BigDecimal getUSTaxRate(String state) {
    if (state == null) {
      return BigDecimal.valueOf(7L); // Default US rate
    }

    // Simplified US state sales tax rates
    return switch (state) {
      case "CA" -> BigDecimal.valueOf(7.25);
      case "NY" -> BigDecimal.valueOf(4L);
      case "TX" -> BigDecimal.valueOf(6.25);
      case "FL" -> BigDecimal.valueOf(6L);
      case "WA" -> BigDecimal.valueOf(6.5);
      default -> BigDecimal.valueOf(7L);
    };
  }
}
