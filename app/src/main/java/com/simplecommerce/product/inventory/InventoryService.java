package com.simplecommerce.product.inventory;

/**
 * Service for managing product variant inventory.
 *
 * @author julius.krah
 */
public interface InventoryService {

  /**
   * Adjust inventory for a variant.
   *
   * @param variantId the variant ID
   * @param adjustment quantity to add (positive) or subtract (negative)
   * @param reason the reason for adjustment
   * @return the new quantity
   */
  int adjustInventory(String variantId, int adjustment, String reason);

  /**
   * Enable or disable inventory tracking for a variant.
   *
   * @param variantId the variant ID
   * @param trackInventory whether to track inventory
   * @param initialQuantity initial quantity if enabling tracking
   * @return the new tracking status
   */
  boolean setInventoryTracking(String variantId, boolean trackInventory, Integer initialQuantity);

  /**
   * Reserve inventory for a variant (e.g., when adding to cart).
   * This decrements the available quantity.
   *
   * @param variantId the variant ID
   * @param quantity quantity to reserve
   * @return true if reservation was successful
   * @throws IllegalStateException if insufficient inventory
   */
  boolean reserveInventory(String variantId, int quantity);

  /**
   * Release reserved inventory for a variant (e.g., when removing from cart).
   * This increments the available quantity.
   *
   * @param variantId the variant ID
   * @param quantity quantity to release
   */
  void releaseInventory(String variantId, int quantity);

  /**
   * Check if a variant has sufficient inventory.
   *
   * @param variantId the variant ID
   * @param quantity required quantity
   * @return true if sufficient inventory is available
   */
  boolean hasAvailableInventory(String variantId, int quantity);
}
