package com.simplecommerce.payment;

/**
 * Represents the status of a refund.
 *
 * @author julius.krah
 */
public enum RefundStatus {
  /** Refund is pending processing */
  PENDING,

  /** Refund is being processed */
  PROCESSING,

  /** Refund has been completed */
  COMPLETED,

  /** Refund has failed */
  FAILED,

  /** Refund has been cancelled */
  CANCELLED
}
