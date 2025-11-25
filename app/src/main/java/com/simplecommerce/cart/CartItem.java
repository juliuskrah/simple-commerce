package com.simplecommerce.cart;

import com.simplecommerce.product.variant.ProductVariant;
import com.simplecommerce.shared.types.Money;
import java.time.OffsetDateTime;

/**
 * An item in a shopping cart.
 *
 * @author julius.krah
 */
public record CartItem(
    String id,
    ProductVariant variant,
    int quantity,
    Money unitPrice,
    Money totalPrice,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
