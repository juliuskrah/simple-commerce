package com.simplecommerce.product;

import com.simplecommerce.shared.exceptions.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
   * Find all categories.
   * @param limit The maximum number of categories to return.
   * @param scroll The scroll position.
   * @return A window of categories.
   */
  Window<Category> findCategories(int limit, ScrollPosition scroll);

  /**
   * Find a category by its product ID.
   * @param productId The title of the category.
   * @return The category when found.
   */
  Optional<Category> findProductCategory(String productId);

  /**
   * Find the parent of a category.
   * @param id The ID of the category.
   * @throws NotFoundException If the category has no parent.
   * @return The parent category if category is not root.
   */
  Optional<Category> findCategoryParent(String id);

  /**
   * Find the parents of multiple categories in batch.
   * @param ids The IDs of the categories.
   * @return The parent categories for each ID, in the same order.
   */
  List<Optional<Category>> findCategoryParents(Set<String> ids);

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
  List<Integer> findCategoryLevels(Set<String> ids);

  /**
   * Check if a category is a leaf node.
   * @param id The ID of the category.
   * @return True if the category is a leaf node.
   */
  boolean isLeaf(String id);

  /**
   * Check if multiple categories are leaf nodes in batch.
   * @param ids The IDs of the categories.
   * @return The leaf status for each ID, in the same order.
   */
  List<Boolean> findCategoryLeafStatus(Set<String> ids);

  /**
   * Check if a category is a root node.
   * @param id The ID of the category.
   * @return True if the category is a root node.
   */
  boolean isRoot(String id);

  /**
   * Check if multiple categories are root nodes in batch.
   * @param ids The IDs of the categories.
   * @return The root status for each ID, in the same order.
   */
  List<Boolean> findCategoryRootStatus(Set<String> ids);

  /**
   * Create a new category.
   * @param input The category input data.
   * @throws IllegalArgumentException If the parent ID is invalid or creates a cycle.
   * @throws NotFoundException If the parent category is not found.
   * @return The created category.
   */
  Category createCategory(CategoryInput input);

  /**
   * Update an existing category.
   * @param id The ID of the category to update.
   * @param input The updated category data.
   * @throws IllegalArgumentException If the global ID is invalid or update creates a cycle.
   * @throws NotFoundException If the category is not found.
   * @return The updated category.
   */
  Category updateCategory(String id, CategoryInput input);

  /**
   * Delete a category.
   * @param id The ID of the category to delete.
   * @throws IllegalArgumentException If the global ID is invalid or category has children.
   * @throws NotFoundException If the category is not found.
   * @return The ID of the deleted category.
   */
  String deleteCategory(String id);

  /**
   * Get the breadcrumb trail for a category.
   * @param id The ID of the category.
   * @return The full breadcrumb path, e.g., "Animals & Pet Supplies > Pet Supplies > Dog Supplies"
   */
  String getCategoryBreadcrumb(String id);

  /**
   * Get the breadcrumb trails for multiple categories in batch.
   * @param ids The IDs of the categories.
   * @return The breadcrumb paths for each ID, in the same order.
   */
  List<String> getCategoryBreadcrumbs(Set<String> ids);
}
