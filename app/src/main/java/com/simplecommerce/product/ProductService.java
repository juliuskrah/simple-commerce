package com.simplecommerce.product;

import com.simplecommerce.shared.types.Product;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import reactor.core.publisher.Mono;

/**
 * @author julius.krah
 */
interface ProductService {

  /**
   * Find a product by its ID.
   * @param id The ID of the product.
   * @see com.simplecommerce.shared.GlobalId#decode(String)
   * @throws IllegalArgumentException If the global ID is invalid.
   * @throws com.simplecommerce.shared.exceptions.NotFoundException If the product is not found.
   * @return The product when found.
   */
  Product findProduct(String id);

  /**
   * Find tags given a product ID.
   * @param productId the id of the product.
   * @param limit the maximum number of tags to return.
   * @return tags of the product
   */
  List<String> findTags(String productId, int limit);

  /**
   * Find tags for a list of products.
   * @param productIds The list of product IDs.
   * @param limit The number of tags to return.
   * @return A list of products with tags.
   */
  List<ProductWithTags> findTags(Set<String> productIds, int limit);

  /**
   * Find a list of products using cursor-based pagination.
   * @param limit The maximum number of products to return.
   * @param sort The sort order.
   * @param scroll The scroll position.
   * @return The slice of products.
   */
  Window<Product> findProducts(int limit, Sort sort, ScrollPosition scroll);

  /**
   * Find a list of products with search query using cursor-based pagination.
   * @param limit The maximum number of products to return.
   * @param sort The sort order.
   * @param scroll The scroll position.
   * @param searchQuery The search query string using GitHub-style syntax.
   * @return The slice of products matching the search criteria.
   */
  Window<Product> findProducts(int limit, Sort sort, ScrollPosition scroll, String searchQuery);

  /**
   * Find a list of products by category using cursor-based pagination.
   * @param categoryId The ID of the category.
   * @param limit The maximum number of products to return.
   * @param sort The sort order.
   * @param scroll The scroll position.
   * @return The slice of products.
   */
  Window<Product> findProductsByCategory(String categoryId, int limit, Sort sort, ScrollPosition scroll);

  /**
   * Delete a product by its ID. Deletion is idempotent.
   * @param id The ID of the product.
   * @see com.simplecommerce.shared.GlobalId#decode(String)
   * @throws IllegalArgumentException If the global ID is invalid.
   * @return The product.
   */
  String deleteProduct(String id);

  /**
   * Create a new product.
   * @param product The product to create.
   * @return The created product.
   */
  Product createProduct(ProductInput product);

  /**
   * Update an existing product.
   * @param productId The ID of the product.
   * @param product The product to update.
   * @see com.simplecommerce.shared.GlobalId#decode(String)
   * @throws IllegalArgumentException If the global ID is invalid.
   * @return The updated product.
   */
  Product updateProduct(String productId, ProductInput product);

  /**
   * Find a product entity by its ID (for state machine operations).
   * @param id The ID of the product.
   * @return The product entity when found, null if not found.
   */
  ProductEntity findProductEntity(String id);

  /**
   * Save a product entity (for state machine operations).
   * @param productEntity The product entity to save.
   * @return The saved product entity.
   */
  ProductEntity saveProductEntity(ProductEntity productEntity);

  /// @param id The ID of the product.
  /// @return The published product.
  Mono<Product> publishProduct(String id);

  /// @param id The ID of the product.
  /// @return The archived product.
  Mono<Product> archiveProduct(String id);

  /// @param id The ID of the product.
  /// @return The reactivated product.
  Mono<Product> reactivateProduct(String id);
}
