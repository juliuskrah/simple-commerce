package com.simplecommerce.order;

import com.simplecommerce.product.variant.ProductVariant;
import com.simplecommerce.shared.types.Money;
import java.time.OffsetDateTime;

/**
 * An item in an order.
 *
 * @author julius.krah
 */
public record OrderItem(
    String id,
    ProductVariant variant,
    String productTitle,
    String variantTitle,
    String sku,
    int quantity,
    Money unitPrice,
    Money totalPrice,
    Money tax,
    Money discount,
    FulfillmentStatus fulfillmentStatus,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
