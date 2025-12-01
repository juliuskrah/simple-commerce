package com.simplecommerce.shipping;

/**
 * Represents the status of a shipment.
 *
 * @author julius.krah
 */
public enum ShipmentStatus {
  /** Shipment is pending creation */
  PENDING,

  /** Shipping label has been created */
  LABEL_CREATED,

  /** Package has been shipped */
  SHIPPED,

  /** Package is in transit */
  IN_TRANSIT,

  /** Package is out for delivery */
  OUT_FOR_DELIVERY,

  /** Package has been delivered */
  DELIVERED,

  /** Delivery failed */
  FAILED,

  /** Shipment was cancelled */
  CANCELLED
}
