package com.simplecommerce.order;

/**
 * Represents the status of an order in its lifecycle.
 *
 * @author julius.krah
 */
public enum OrderStatus {
  /** Order created but not yet confirmed by customer */
  PENDING,

  /** Order confirmed by customer, payment authorized */
  CONFIRMED,

  /** Order is being processed (picking, packing) */
  PROCESSING,

  /** Order items have been fulfilled (ready to ship or delivered for digital) */
  FULFILLED,

  /** Order has been shipped to customer */
  SHIPPED,

  /** Order has been delivered to customer */
  DELIVERED,

  /** Order has been cancelled */
  CANCELLED,

  /** Order payment has been refunded */
  REFUNDED
}
