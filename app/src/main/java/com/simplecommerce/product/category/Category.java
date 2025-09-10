package com.simplecommerce.product.category;

import com.simplecommerce.node.Node;
import java.time.OffsetDateTime;

/**
 * @author julius.krah
 */
public record Category(
    String id,
    String title,
    String slug,
    OffsetDateTime createdAt,
    String description,
    OffsetDateTime updatedAt
) implements Node {

}
