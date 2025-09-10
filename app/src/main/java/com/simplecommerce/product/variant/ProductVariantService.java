package com.simplecommerce.product.variant;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

/**
 * Service for managing product variants.
 * @author julius.krah
 */
public interface ProductVariantService {

  /**
   * Find a product variant by its ID.
   * @param id the variant ID
   * @return the product variant
   */
  ProductVariant findVariant(String id);

  /**
   * Find variants for a product.
   * @param productId the product ID
   * @param limit the maximum number of variants to return
   * @param sort the sort criteria
   * @param scroll the scroll position for pagination
   * @return a window of variants
   */
  Window<ProductVariant> findVariantsByProduct(String productId, int limit, Sort sort, ScrollPosition scroll);

  /**
   * Create a new product variant.
   * @param productId the product ID
   * @param input the variant input
   * @return the created variant
   */
  ProductVariant createVariant(String productId, ProductVariantInput input);

  /**
   * Update an existing product variant.
   * @param id the variant ID
   * @param input the variant input
   * @return the updated variant
   */
  ProductVariant updateVariant(String id, ProductVariantInput input);

  /**
   * Delete a product variant.
   * @param id the variant ID
   * @return the deleted variant ID
   */
  String deleteVariant(String id);
}
