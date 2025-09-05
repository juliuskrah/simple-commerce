package com.simplecommerce.product;

import com.simplecommerce.node.Node;
import java.time.OffsetDateTime;

/**
 * Represents a product.
 * @author julius.krah
 */
public record Product(
    String id,
    String title,
    String slug,
    OffsetDateTime createdAt,
    String description,
    OffsetDateTime updatedAt,
    ProductStatus status
) implements Node {

}
