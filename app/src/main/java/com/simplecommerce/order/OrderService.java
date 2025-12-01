package com.simplecommerce.order;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for managing orders.
 *
 * @author julius.krah
 */
public interface OrderService {

  /**
   * Create an order from a cart (checkout).
   *
   * @param input the checkout input
   * @return the created order
   */
  Order checkout(CheckoutInput input);

  /**
   * Get an order by its ID.
   *
   * @param id the order ID
   * @return the order
   */
  Order findById(String id);

  /**
   * Get an order by its order number.
   *
   * @param orderNumber the order number
   * @return the order
   */
  Order findByOrderNumber(String orderNumber);

  /**
   * Get all orders for the current customer.
   *
   * @param pageable pagination information
   * @return page of orders
   */
  Page<Order> getCustomerOrders(Pageable pageable);

  /**
   * Get all orders for a specific customer (admin only).
   *
   * @param customerId the customer ID
   * @param pageable pagination information
   * @return page of orders
   */
  Page<Order> getOrdersByCustomerId(String customerId, Pageable pageable);

  /**
   * Get all orders.
   *
   * @param pageable pagination information
   * @return page of orders
   */
  Page<Order> getAllOrders(Pageable pageable);

  /**
   * Get orders by status.
   *
   * @param statuses the list of statuses
   * @param pageable pagination information
   * @return page of orders
   */
  Page<Order> getOrdersByStatus(List<OrderStatus> statuses, Pageable pageable);

  /**
   * Update order status.
   *
   * @param id the order ID
   * @param status the new status
   * @return the updated order
   */
  Order updateOrderStatus(String id, OrderStatus status);

  /**
   * Cancel an order.
   *
   * @param id the order ID
   * @param reason the cancellation reason
   * @return the cancelled order
   */
  Order cancelOrder(String id, String reason);

  /**
   * Confirm an order (mark as confirmed after payment).
   *
   * @param id the order ID
   * @return the confirmed order
   */
  Order confirmOrder(String id);

  /**
   * Mark an order as fulfilled.
   *
   * @param id the order ID
   * @return the fulfilled order
   */
  Order fulfillOrder(String id);
}
