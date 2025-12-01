package com.simplecommerce.order;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link OrderItemEntity}.
 *
 * @author julius.krah
 */
public interface OrderItems extends JpaRepository<OrderItemEntity, UUID> {

  /**
   * Find all items for an order.
   *
   * @param orderId the order ID
   * @return list of order items
   */
  @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
  List<OrderItemEntity> findByOrderId(@Param("orderId") UUID orderId);

  /**
   * Find all items with a specific fulfillment status.
   *
   * @param orderId the order ID
   * @param status the fulfillment status
   * @return list of order items
   */
  @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.fulfillmentStatus = :status")
  List<OrderItemEntity> findByOrderIdAndFulfillmentStatus(
      @Param("orderId") UUID orderId,
      @Param("status") FulfillmentStatus status
  );
}
