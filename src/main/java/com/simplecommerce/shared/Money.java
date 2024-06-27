package com.simplecommerce.shared;

import java.math.BigDecimal;

/**
 * @param currency currency unit
 * @param amount amount
 *
 * @author julius.krah
 */
public record Money(String currency, BigDecimal amount) {

}
