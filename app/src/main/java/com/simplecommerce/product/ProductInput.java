package com.simplecommerce.product;

import com.simplecommerce.shared.types.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author julius.krah
 */
record ProductInput(
    String description,
    BigDecimal price,
    List<String> tags,
    String title,
    ProductStatus status
) {

}
