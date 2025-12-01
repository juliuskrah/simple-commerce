package com.simplecommerce.payment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link PaymentEntity}.
 *
 * @author julius.krah
 */
public interface Payments extends JpaRepository<PaymentEntity, UUID> {

  /**
   * Find all payments for an order.
   *
   * @param orderId the order ID
   * @return list of payments
   */
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC")
  List<PaymentEntity> findByOrderId(@Param("orderId") UUID orderId);

  /**
   * Find payments by status.
   *
   * @param status the payment status
   * @return list of payments
   */
  List<PaymentEntity> findByStatus(PaymentStatus status);
}
