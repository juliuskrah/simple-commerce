package com.simplecommerce.product.variant;

import java.math.BigDecimal;
import javax.money.CurrencyUnit;

/**
 * Input for creating or updating a product variant.
 * @author julius.krah
 */
public record ProductVariantInput(
    String sku,
    String title,
    MoneyInput price
) {
}

/**
 * Input for money values.
 */
record MoneyInput(
    BigDecimal amount,
    CurrencyUnit currency
) {
}
