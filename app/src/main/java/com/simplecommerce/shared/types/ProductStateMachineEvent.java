package com.simplecommerce.shared.types;

/**
 * Events that trigger state transitions in the Product lifecycle state machine.
 * These are different from domain events - they represent state machine triggers.
 * 
 * @author julius.krah
 * @since 1.0
 */
public enum ProductStateMachineEvent {
  /**
   * Event to publish a draft product.
   * Transition: DRAFT → PUBLISHED
   * Preconditions:
   * - Product must have at least one variant with valid pricing
   * - All required fields must be filled
   */
  PUBLISH,
  
  /**
   * Event to archive a published product.
   * Transition: PUBLISHED → ARCHIVED
   * Preconditions:
   * - Handle existing orders and inventory
   * - Notify stakeholders of product discontinuation
   */
  ARCHIVE,
  
  /**
   * Event to reactivate an archived product.
   * Transition: ARCHIVED → DRAFT
   * This allows products to be brought back from archive
   * but requires re-validation before publishing.
   */
  REACTIVATE
}