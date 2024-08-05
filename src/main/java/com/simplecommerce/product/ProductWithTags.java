package com.simplecommerce.product;

import java.util.List;
import java.util.UUID;

/**
 * @author julius.krah
 */
public interface ProductWithTags {
  UUID getId();
  List<String> getTags();
}
