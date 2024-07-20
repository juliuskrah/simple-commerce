package com.simplecommerce.product;

import java.util.List;

/**
 * @author julius.krah
 */
interface ProductService {

  /**
   * Find a product by its ID.
   * @param id The ID of the product.
   * @return The product.
   */
  Product findProduct(String id);

  /**
   * Find a list of products.
   * @param limit The maximum number of products to return.
   * @return The list of products.
   */
  List<Product> findProducts(int limit);
  /**
   * Delete a product by its ID. Deletion is idempotent.
   * @param id The ID of the product.
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
   * @return The updated product.
   */
  Product updateProduct(String productId, ProductInput product);
}
