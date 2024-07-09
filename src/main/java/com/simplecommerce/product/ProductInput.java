package com.simplecommerce.product;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author julius.krah
 */
record ProductInput(
    String description,
    BigDecimal price,
    List<String> tags,
    String title
) {

}
