package com.simplecommerce.product.pricing;

import javax.money.CurrencyUnit;

/**
 * Input for price context.
 *
 * @since 1.0
 * @author julius.krah
 */
public record PriceContextInput(String customerGroup, String region, CurrencyUnit currency, Integer quantity) {

}
