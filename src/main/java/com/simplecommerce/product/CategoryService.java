package com.simplecommerce.product;

import com.simplecommerce.shared.NotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

/**
 * @author julius.krah
 */
interface CategoryService {

  /**
   * Find a category by its ID.
   * @param id The ID of the category.
   * @throws IllegalArgumentException If the global ID is invalid.
   * @throws NotFoundException If the category is not found.
   * @see com.simplecommerce.shared.GlobalId#decode(String)
   * @return The category when found.
   */
  Category findCategory(String id);

  /**
   * Find the parent of a category.
   * @param id The ID of the category.
   * @throws NotFoundException If the category has no parent.
   * @return The parent category if category is not root.
   */
  Optional<Category> findCategoryParent(String id);

  /**
   * Find the ancestors of a category.
   * @param id The ID of the category.
   * @param limit The maximum number of ancestors to return.
   * @param scroll The scroll position.
   * @return The ancestors of the category.
   */
  Window<Category> findCategoryAncestors(String id, int limit, ScrollPosition scroll);

  /**
   * Find the descendants of a category.
   * @param id The ID of the category.
   * @param limit The maximum number of descendants to return.
   * @param scroll The scroll position.
   * @return The descendants of the category.
   */
  Window<Category> findCategoryDescendants(String id, int limit, ScrollPosition scroll);

  /**
   * Find the level of a category in the category tree.
   * @param ids The IDs of the categories.
   * @return The level of the category.
   */
  Stream<Integer> findCategoryLevels(Set<String> ids);

  /**
   * Check if a category is a leaf node.
   * @param id The ID of the category.
   * @return True if the category is a leaf node.
   */
  boolean isLeaf(String id);

  /**
   * Check if a category is a root node.
   * @param id The ID of the category.
   * @return True if the category is a root node.
   */
  boolean isRoot(String id);
}
