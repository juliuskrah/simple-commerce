package com.simplecommerce.product;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author julius.krah
 */
interface CategoryService {

  /**
   * Find a category by its ID.
   * @param id The ID of the category.
   * @throws IllegalArgumentException If the global ID is invalid.
   * @see com.simplecommerce.shared.GlobalId#decode(String)
   * @return The category.
   */
  Category findCategory(String id);

  /**
   * Find the level of a category in the category tree.
   * @param ids The IDs of the categories.
   * @return The level of the category.
   */
  Stream<Integer> findCategoryLevels(Set<String> ids);
}
