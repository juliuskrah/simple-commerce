package com.simplecommerce.shared;

import java.util.Locale;
import javax.money.CurrencyQueryBuilder;
import javax.money.CurrencyUnit;
import javax.money.Monetary;

/**
 * @author julius.krah
 */
public final class MonetaryUtils {
  private MonetaryUtils() {}

  /**
   * Get currency unit.
   *
   * @param code currency code
   * @param locale locale
   * @return currency unit
   */
  public static CurrencyUnit getCurrency(String code, Locale locale) {
    var query = CurrencyQueryBuilder.of().setCurrencyCodes(code).setCountries(locale).build();
    return Monetary.getCurrency(query);
  }
}
