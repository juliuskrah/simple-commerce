package com.simplecommerce.product.pricing;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author julius.krah
 */
public record PriceSet(
    String id,
    String name,
    int priority,
    boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<Object> rules
    ) {

}
