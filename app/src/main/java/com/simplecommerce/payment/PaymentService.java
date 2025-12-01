package com.simplecommerce.payment;

import java.util.List;

/**
 * Service for managing payments.
 *
 * @author julius.krah
 */
public interface PaymentService {

  /**
   * Create a payment for an order.
   *
   * @param input the payment input
   * @return the created payment
   */
  Payment createPayment(CreatePaymentInput input);

  /**
   * Get a payment by ID.
   *
   * @param id the payment ID
   * @return the payment
   */
  Payment getPayment(String id);

  /**
   * Get all payments for an order.
   *
   * @param orderId the order ID
   * @return list of payments
   */
  List<Payment> getPaymentsByOrder(String orderId);

  /**
   * Capture an authorized payment.
   *
   * @param id the payment ID
   * @return the captured payment
   */
  Payment capturePayment(String id);

  /**
   * Refund a payment.
   *
   * @param id the payment ID
   * @param amount the amount to refund (optional, full refund if not specified)
   * @param reason the refund reason
   * @return the refunded payment
   */
  Payment refundPayment(String id, String amount, String reason);
}
