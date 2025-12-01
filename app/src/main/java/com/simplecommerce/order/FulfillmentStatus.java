package com.simplecommerce.order;

/**
 * Represents the fulfillment status of an order item.
 *
 * @author julius.krah
 */
public enum FulfillmentStatus {
  /** Item has not been fulfilled */
  UNFULFILLED,

  /** Item has been partially fulfilled */
  PARTIAL,

  /** Item has been completely fulfilled */
  FULFILLED,

  /** Item has been returned by customer */
  RETURNED,

  /** Item fulfillment has been cancelled */
  CANCELLED
}
