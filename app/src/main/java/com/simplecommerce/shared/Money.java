package com.simplecommerce.shared;

import java.math.BigDecimal;
import javax.money.CurrencyUnit;

/**
 * @param currency currency unit
 * @param amount amount
 *
 * @author julius.krah
 */
public record Money(CurrencyUnit currency, BigDecimal amount) {

}
