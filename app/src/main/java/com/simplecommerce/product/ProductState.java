package com.simplecommerce.product;

/**
 * States for the Product lifecycle state machine.
 * Product Lifecycle:
 * DRAFT → PUBLISHED → ARCHIVED
 * 
 * @author julius.krah
 * @since 1.0
 */
public enum ProductState {
  /**
   * Initial state when a product is created.
   * Product is not visible to customers and can be freely edited.
   */
  DRAFT,
  
  /**
   * Product is live and visible to customers.
   * Changes require validation and may affect live sales.
   */
  PUBLISHED,
  
  /**
   * Product is no longer active but preserved for historical purposes.
   * Not visible to customers, cannot be purchased.
   */
  ARCHIVED
}