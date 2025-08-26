package com.simplecommerce.product;

import com.simplecommerce.node.Node;
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
    String title
) implements Node {

}
