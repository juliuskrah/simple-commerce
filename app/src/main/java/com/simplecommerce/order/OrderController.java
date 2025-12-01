package com.simplecommerce.order;

import com.simplecommerce.actor.User;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * Controller for order operations.
 *
 * @author julius.krah
 * @since 1.0
 */
@Controller
class OrderController {

  private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

  private final ObjectProvider<OrderService> orderService;
  private final Supplier<OrderService> orderServiceSupplier = SingletonSupplier.of(OrderManagement::new);

  OrderController(ObjectProvider<OrderService> orderService) {
    this.orderService = orderService;
  }

  @QueryMapping
  Optional<Order> order(@Argument String id) {
    LOG.debug("Fetching order by id: {}", id);
    try {
      return Optional.of(orderService.getIfAvailable(orderServiceSupplier).findById(id));
    } catch (Exception e) {
      LOG.warn("Order not found: {}", id, e);
      return Optional.empty();
    }
  }

  @QueryMapping
  Optional<Order> orderByNumber(@Argument String orderNumber) {
    LOG.debug("Fetching order by number: {}", orderNumber);
    try {
      return Optional.of(orderService.getIfAvailable(orderServiceSupplier).findByOrderNumber(orderNumber));
    } catch (Exception e) {
      LOG.warn("Order not found: {}", orderNumber, e);
      return Optional.empty();
    }
  }

  @QueryMapping
  Page<Order> myOrders(
      @Argument Integer first,
      @Argument String after,
      @Argument Integer last,
      @Argument String before) {
    LOG.debug("Fetching current customer's orders");

    // For simplicity, using first for page size
    int pageSize = first != null ? first : 10;
    var pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    return orderService.getIfAvailable(orderServiceSupplier).getCustomerOrders(pageable);
  }

  @QueryMapping
  Page<Order> customerOrders(
      @Argument String customerId,
      @Argument Integer first,
      @Argument String after,
      @Argument Integer last,
      @Argument String before) {
    LOG.debug("Fetching orders for customer: {}", customerId);

    int pageSize = first != null ? first : 10;
    var pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    return orderService.getIfAvailable(orderServiceSupplier)
        .getOrdersByCustomerId(customerId, pageable);
  }

  @QueryMapping
  Page<Order> orders(
      @Argument Integer first,
      @Argument String after,
      @Argument Integer last,
      @Argument String before,
      @Argument List<OrderStatus> statuses) {
    LOG.debug("Fetching all orders");

    int pageSize = first != null ? first : 10;
    var pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    if (statuses != null && !statuses.isEmpty()) {
      return orderService.getIfAvailable(orderServiceSupplier)
          .getOrdersByStatus(statuses, pageable);
    }

    return orderService.getIfAvailable(orderServiceSupplier).getAllOrders(pageable);
  }

  @SchemaMapping(typeName = "Order")
  String id(Order source) {
    return source.id();
  }

  @SchemaMapping(typeName = "Order")
  User customer(Order source) {
    return source.customer();
  }

  @SchemaMapping(typeName = "Order")
  Address shippingAddress(Order source) {
    return source.shippingAddress();
  }

  @SchemaMapping(typeName = "Order")
  Address billingAddress(Order source) {
    return source.billingAddress();
  }

  @SchemaMapping(typeName = "OrderItem")
  String id(OrderItem source) {
    return source.id();
  }

  @SchemaMapping(typeName = "Address")
  String fullName(Address source) {
    return source.fullName();
  }

  @MutationMapping
  Order checkout(@Argument CheckoutInput input) {
    LOG.info("Processing checkout for email: {}", input.customerEmail());
    return orderService.getIfAvailable(orderServiceSupplier).checkout(input);
  }

  @MutationMapping
  Order updateOrderStatus(@Argument String id, @Argument OrderStatus status) {
    LOG.info("Updating order status: id={}, status={}", id, status);
    return orderService.getIfAvailable(orderServiceSupplier).updateOrderStatus(id, status);
  }

  @MutationMapping
  Order cancelOrder(@Argument String id, @Argument String reason) {
    LOG.info("Cancelling order: id={}, reason={}", id, reason);
    return orderService.getIfAvailable(orderServiceSupplier).cancelOrder(id, reason);
  }

  @MutationMapping
  Order confirmOrder(@Argument String id) {
    LOG.info("Confirming order: id={}", id);
    return orderService.getIfAvailable(orderServiceSupplier).confirmOrder(id);
  }

  @MutationMapping
  Order fulfillOrder(@Argument String id) {
    LOG.info("Fulfilling order: id={}", id);
    return orderService.getIfAvailable(orderServiceSupplier).fulfillOrder(id);
  }
}
