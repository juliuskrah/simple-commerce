package com.simplecommerce.order;

import com.simplecommerce.actor.ActorEntity;
import com.simplecommerce.actor.Actors;
import com.simplecommerce.actor.User;
import com.simplecommerce.cart.CartEntity;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.types.UserType;
import com.simplecommerce.shared.utils.MonetaryUtils;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of OrderService for managing orders.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
class OrderManagement implements OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderManagement.class);

  private Orders orderRepository;
  private OrderItems orderItemRepository;
  private Actors actorRepository;

  public void setOrderRepository(ObjectFactory<Orders> orderRepository) {
    this.orderRepository = orderRepository.getObject();
  }

  public void setOrderItemRepository(ObjectFactory<OrderItems> orderItemRepository) {
    this.orderItemRepository = orderItemRepository.getObject();
  }

  public void setActorRepository(ObjectFactory<Actors> actorRepository) {
    this.actorRepository = actorRepository.getObject();
  }

  @Override
  public Order checkout(CheckoutInput input) {
    LOG.debug("Processing checkout: email={}", input.customerEmail());

    // Get current user
    var currentUser = getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("User must be authenticated to checkout");
    }

    // TODO: Implement cart-to-order conversion
    // For now, create a minimal order without cart items
    // This will be completed once we can properly access the cart service

    // Create order entity
    var order = new OrderEntity();
    order.setOrderNumber(generateOrderNumber());
    order.setCustomer(currentUser);
    order.setStatus(OrderStatus.PENDING);

    // Set customer information
    order.setCustomerEmail(input.customerEmail());
    order.setCustomerName(input.customerName());

    // Set shipping address
    if (input.shippingAddress() != null) {
      var shipping = input.shippingAddress();
      order.setShippingFirstName(shipping.firstName());
      order.setShippingLastName(shipping.lastName());
      order.setShippingCompany(shipping.company());
      order.setShippingAddressLine1(shipping.addressLine1());
      order.setShippingAddressLine2(shipping.addressLine2());
      order.setShippingCity(shipping.city());
      order.setShippingState(shipping.state());
      order.setShippingPostalCode(shipping.postalCode());
      order.setShippingCountry(shipping.country());
      order.setShippingPhone(shipping.phone());
    }

    // Set billing address
    if (input.billingAddress() != null) {
      var billing = input.billingAddress();
      order.setBillingFirstName(billing.firstName());
      order.setBillingLastName(billing.lastName());
      order.setBillingCompany(billing.company());
      order.setBillingAddressLine1(billing.addressLine1());
      order.setBillingAddressLine2(billing.addressLine2());
      order.setBillingCity(billing.city());
      order.setBillingState(billing.state());
      order.setBillingPostalCode(billing.postalCode());
      order.setBillingCountry(billing.country());
      order.setBillingPhone(billing.phone());
    }

    order.setCustomerNotes(input.customerNotes());
    order.setPlacedAt(OffsetDateTime.now());

    // Set order financials (placeholder values)
    String currency = "USD";
    BigDecimal subtotal = BigDecimal.ZERO;

    order.setSubtotalAmount(subtotal);
    order.setSubtotalCurrency(currency);
    order.setTaxAmount(BigDecimal.ZERO);
    order.setTaxCurrency(currency);
    order.setShippingAmount(BigDecimal.ZERO);
    order.setShippingCurrency(currency);
    order.setDiscountAmount(BigDecimal.ZERO);
    order.setDiscountCurrency(currency);
    order.setTotalAmount(subtotal);
    order.setTotalCurrency(currency);

    // Save order
    order = orderRepository.save(order);

    LOG.info("Order created: orderNumber={}, orderId={}", order.getOrderNumber(), order.getId());
    return toOrder(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Order findById(String id) {
    var globalId = GlobalId.decode(id);
    var order = orderRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Order not found"));
    return toOrder(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Order findByOrderNumber(String orderNumber) {
    var order = orderRepository.findByOrderNumber(orderNumber)
        .orElseThrow(() -> new NotFoundException("Order not found"));
    return toOrder(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> getCustomerOrders(Pageable pageable) {
    var currentUser = getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("User must be authenticated");
    }
    return orderRepository.findByCustomerId(currentUser.getId(), pageable)
        .map(this::toOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> getOrdersByCustomerId(String customerId, Pageable pageable) {
    var globalId = GlobalId.decode(customerId);
    return orderRepository.findByCustomerId(UUID.fromString(globalId.id()), pageable)
        .map(this::toOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> getAllOrders(Pageable pageable) {
    return orderRepository.findAll(pageable)
        .map(this::toOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> getOrdersByStatus(List<OrderStatus> statuses, Pageable pageable) {
    return orderRepository.findByStatusIn(statuses, pageable)
        .map(this::toOrder);
  }

  @Override
  public Order updateOrderStatus(String id, OrderStatus status) {
    var globalId = GlobalId.decode(id);
    var order = orderRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Order not found"));

    order.setStatus(status);

    // Update timestamps based on status
    var now = OffsetDateTime.now();
    switch (status) {
      case CONFIRMED -> order.setConfirmedAt(now);
      case FULFILLED -> order.setFulfilledAt(now);
      case CANCELLED -> order.setCancelledAt(now);
      default -> {}
    }

    order = orderRepository.save(order);
    LOG.info("Order status updated: orderNumber={}, status={}", order.getOrderNumber(), status);
    return toOrder(order);
  }

  @Override
  public Order cancelOrder(String id, String reason) {
    var globalId = GlobalId.decode(id);
    var order = orderRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Order not found"));

    if (order.getStatus() == OrderStatus.DELIVERED ||
        order.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
    }

    order.setStatus(OrderStatus.CANCELLED);
    order.setCancelledAt(OffsetDateTime.now());
    if (reason != null) {
      order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") +
          "Cancellation reason: " + reason);
    }

    order = orderRepository.save(order);
    LOG.info("Order cancelled: orderNumber={}", order.getOrderNumber());
    return toOrder(order);
  }

  @Override
  public Order confirmOrder(String id) {
    return updateOrderStatus(id, OrderStatus.CONFIRMED);
  }

  @Override
  public Order fulfillOrder(String id) {
    return updateOrderStatus(id, OrderStatus.FULFILLED);
  }

  /**
   * Generate a unique order number.
   */
  private String generateOrderNumber() {
    // Format: ORD-YYYYMMDD-XXXX
    var date = OffsetDateTime.now();
    var dateStr = String.format("%04d%02d%02d",
        date.getYear(), date.getMonthValue(), date.getDayOfMonth());

    // Get next sequence number
    var seq = orderRepository.count() + 1000; // Start from 1000
    return String.format("ORD-%s-%04d", dateStr, seq);
  }

  private ActorEntity getCurrentUser() {
    // TODO: Implement proper security context access
    // For now, return null which will cause checkout to fail with clear message
    return null;
  }

  private Order toOrder(OrderEntity entity) {
    var customer = toUser(entity.getCustomer());

    var shippingAddress = new Address(
        entity.getShippingFirstName(),
        entity.getShippingLastName(),
        entity.getShippingCompany(),
        entity.getShippingAddressLine1(),
        entity.getShippingAddressLine2(),
        entity.getShippingCity(),
        entity.getShippingState(),
        entity.getShippingPostalCode(),
        entity.getShippingCountry(),
        entity.getShippingPhone()
    );

    var billingAddress = new Address(
        entity.getBillingFirstName(),
        entity.getBillingLastName(),
        entity.getBillingCompany(),
        entity.getBillingAddressLine1(),
        entity.getBillingAddressLine2(),
        entity.getBillingCity(),
        entity.getBillingState(),
        entity.getBillingPostalCode(),
        entity.getBillingCountry(),
        entity.getBillingPhone()
    );

    var items = entity.getItems().stream()
        .map(this::toOrderItem)
        .toList();

    return new Order(
        new GlobalId("Order", entity.getId().toString()).encode(),
        entity.getOrderNumber(),
        customer,
        entity.getStatus(),
        toMoney(entity.getSubtotalCurrency(), entity.getSubtotalAmount()),
        toMoney(entity.getTaxCurrency(), entity.getTaxAmount()),
        toMoney(entity.getShippingCurrency(), entity.getShippingAmount()),
        toMoney(entity.getDiscountCurrency(), entity.getDiscountAmount()),
        toMoney(entity.getTotalCurrency(), entity.getTotalAmount()),
        entity.getCustomerEmail(),
        entity.getCustomerName(),
        shippingAddress,
        billingAddress,
        entity.getNotes(),
        entity.getCustomerNotes(),
        items,
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getPlacedAt(),
        entity.getConfirmedAt(),
        entity.getFulfilledAt(),
        entity.getCancelledAt()
    );
  }

  private OrderItem toOrderItem(OrderItemEntity entity) {
    // TODO: Implement full ProductVariant conversion
    return new OrderItem(
        new GlobalId("OrderItem", entity.getId().toString()).encode(),
        null, // variant - simplified for now
        entity.getProductTitle(),
        entity.getVariantTitle(),
        entity.getSku(),
        entity.getQuantity(),
        toMoney(entity.getUnitPriceCurrency(), entity.getUnitPriceAmount()),
        toMoney(entity.getTotalPriceCurrency(), entity.getTotalPriceAmount()),
        toMoney(entity.getTaxCurrency(), entity.getTaxAmount()),
        toMoney(entity.getDiscountCurrency(), entity.getDiscountAmount()),
        entity.getFulfillmentStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }

  private User toUser(ActorEntity entity) {
    return new User(
        new GlobalId("User", entity.getId().toString()).encode(),
        entity.getUsername(),
        UserType.CUSTOMER, // Default for now
        null, // updatedAt - not available in ActorEntity
        null, // createdAt - not available in ActorEntity
        null, // lastLogin - not available in ActorEntity
        entity.getEmail()
    );
  }

  private com.simplecommerce.shared.types.Money toMoney(String currencyCode, BigDecimal amount) {
    var currency = MonetaryUtils.getCurrency(currencyCode, java.util.Locale.getDefault());
    return new com.simplecommerce.shared.types.Money(currency, amount);
  }
}
