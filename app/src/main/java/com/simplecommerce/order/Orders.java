package com.simplecommerce.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link OrderEntity}.
 *
 * @author julius.krah
 */
public interface Orders extends JpaRepository<OrderEntity, UUID> {

  /**
   * Find an order by its order number.
   *
   * @param orderNumber the order number
   * @return the order if found
   */
  Optional<OrderEntity> findByOrderNumber(String orderNumber);

  /**
   * Find all orders for a customer.
   *
   * @param customerId the customer ID
   * @param pageable pagination information
   * @return page of orders
   */
  @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
  Page<OrderEntity> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

  /**
   * Find orders by status.
   *
   * @param status the order status
   * @param pageable pagination information
   * @return page of orders
   */
  Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

  /**
   * Find orders by customer email.
   *
   * @param email the customer email
   * @param pageable pagination information
   * @return page of orders
   */
  Page<OrderEntity> findByCustomerEmailIgnoreCase(String email, Pageable pageable);

  /**
   * Count orders by customer.
   *
   * @param customerId the customer ID
   * @return the count of orders
   */
  @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId")
  long countByCustomerId(@Param("customerId") UUID customerId);

  /**
   * Find all orders with a specific list of statuses.
   *
   * @param statuses the list of statuses
   * @param pageable pagination information
   * @return page of orders
   */
  Page<OrderEntity> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);
}
