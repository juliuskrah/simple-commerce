package com.simplecommerce.payment;

/**
 * Represents the status of a payment.
 *
 * @author julius.krah
 */
public enum PaymentStatus {
  /** Payment is pending processing */
  PENDING,

  /** Payment has been authorized but not captured */
  AUTHORIZED,

  /** Payment has been captured/completed */
  CAPTURED,

  /** Payment has been partially refunded */
  PARTIALLY_REFUNDED,

  /** Payment has been fully refunded */
  REFUNDED,

  /** Payment has failed */
  FAILED,

  /** Payment has been cancelled */
  CANCELLED
}
