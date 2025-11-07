package com.simplecommerce.shared.types;

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
    String createdBy,
    String description,
    OffsetDateTime updatedAt,
    String updatedBy,
    ProductStatus status
) implements Node {

}
