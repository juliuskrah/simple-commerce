package com.simplecommerce.product;

import com.simplecommerce.shared.Node;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents a product.
 * @author julius.krah
 */
record Product(
    String id,
    String title,
    String slug,
    OffsetDateTime createdAt,
    String description,
    List<String> tags,
    OffsetDateTime updatedAt
) implements Node {

  @Override
  public String id() {
    return id;
  }
}
