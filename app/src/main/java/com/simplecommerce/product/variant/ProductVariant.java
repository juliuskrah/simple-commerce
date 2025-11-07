package com.simplecommerce.product.variant;

import com.simplecommerce.shared.types.Node;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Represents a product variant.
 * @author julius.krah
 */
public record ProductVariant(
    String id,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String productId,
    String sku,
    String title,
    BigDecimal priceAmount,
    String priceCurrency
) implements Node {

}
