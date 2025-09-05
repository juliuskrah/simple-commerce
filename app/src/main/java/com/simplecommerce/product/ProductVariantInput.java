package com.simplecommerce.product;

import java.math.BigDecimal;

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
    String currency
) {
}
