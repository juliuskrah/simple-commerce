package com.simplecommerce.shared.utils;

import java.math.BigDecimal;
import java.util.Locale;
import javax.money.CurrencyQueryBuilder;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;

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

  /**
   * Get currency unit.
   *
   * @param locale locale
   * @return currency unit
   */
  public static CurrencyUnit getCurrency(Locale locale) {
    var query = CurrencyQueryBuilder.of().setCountries(locale).build();
    return Monetary.getCurrency(query);
  }

  /**
   * Get monetary amount.
   *
   * @param amount amount
   * @param currency currency unit
   * @return monetary amount
   */
  public static MonetaryAmount getMonetaryAmount(BigDecimal amount, CurrencyUnit currency) {
    MonetaryAmountFactory<?> amountFactory = Monetary.getDefaultAmountFactory();
    return amountFactory.setCurrency(currency)
        .setNumber(amount)
        .create();
  }

  /**
   * Get monetary amount.
   *
   * @param amount amount
   * @param code currency code
   * @param locale locale
   * @return monetary amount
   */
  public static MonetaryAmount getMonetaryAmount(BigDecimal amount, String code, Locale locale) {
    return getMonetaryAmount(amount, getCurrency(code, locale));
  }
}
