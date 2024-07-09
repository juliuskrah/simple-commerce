package com.simplecommerce.product;

/**
 * @author julius.krah
 */
interface ProductService {
  /**
   * Delete a product by its ID. This method is idempotent.
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
