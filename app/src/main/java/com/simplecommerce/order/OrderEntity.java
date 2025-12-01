package com.simplecommerce.order;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.actor.ActorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

/**
 * An order represents a customer's purchase.
 *
 * @author julius.krah
 */
@Entity(name = "Order")
@Table(name = "orders")
public class OrderEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private ActorEntity customer;

  @Enumerated(STRING)
  @Column(nullable = false, length = 50)
  private OrderStatus status = OrderStatus.PENDING;

  // Financial details
  @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal subtotalAmount;

  @Column(name = "subtotal_currency", nullable = false, length = 3)
  private String subtotalCurrency;

  @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "tax_currency", nullable = false, length = 3)
  private String taxCurrency;

  @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal shippingAmount = BigDecimal.ZERO;

  @Column(name = "shipping_currency", nullable = false, length = 3)
  private String shippingCurrency;

  @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "discount_currency", nullable = false, length = 3)
  private String discountCurrency;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal totalAmount;

  @Column(name = "total_currency", nullable = false, length = 3)
  private String totalCurrency;

  // Customer information
  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(name = "customer_name")
  @Nullable
  private String customerName;

  // Shipping address
  @Column(name = "shipping_first_name", length = 100)
  @Nullable
  private String shippingFirstName;

  @Column(name = "shipping_last_name", length = 100)
  @Nullable
  private String shippingLastName;

  @Column(name = "shipping_company", length = 100)
  @Nullable
  private String shippingCompany;

  @Column(name = "shipping_address_line1")
  @Nullable
  private String shippingAddressLine1;

  @Column(name = "shipping_address_line2")
  @Nullable
  private String shippingAddressLine2;

  @Column(name = "shipping_city", length = 100)
  @Nullable
  private String shippingCity;

  @Column(name = "shipping_state", length = 100)
  @Nullable
  private String shippingState;

  @Column(name = "shipping_postal_code", length = 20)
  @Nullable
  private String shippingPostalCode;

  @Column(name = "shipping_country", length = 2)
  @Nullable
  private String shippingCountry;

  @Column(name = "shipping_phone", length = 20)
  @Nullable
  private String shippingPhone;

  // Billing address
  @Column(name = "billing_first_name", length = 100)
  @Nullable
  private String billingFirstName;

  @Column(name = "billing_last_name", length = 100)
  @Nullable
  private String billingLastName;

  @Column(name = "billing_company", length = 100)
  @Nullable
  private String billingCompany;

  @Column(name = "billing_address_line1")
  @Nullable
  private String billingAddressLine1;

  @Column(name = "billing_address_line2")
  @Nullable
  private String billingAddressLine2;

  @Column(name = "billing_city", length = 100)
  @Nullable
  private String billingCity;

  @Column(name = "billing_state", length = 100)
  @Nullable
  private String billingState;

  @Column(name = "billing_postal_code", length = 20)
  @Nullable
  private String billingPostalCode;

  @Column(name = "billing_country", length = 2)
  @Nullable
  private String billingCountry;

  @Column(name = "billing_phone", length = 20)
  @Nullable
  private String billingPhone;

  // Metadata
  @Column(columnDefinition = "TEXT")
  @Nullable
  private String notes;

  @Column(name = "customer_notes", columnDefinition = "TEXT")
  @Nullable
  private String customerNotes;

  @Column(name = "ip_address", length = 45)
  @Nullable
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  @Nullable
  private String userAgent;

  @OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)
  private List<OrderItemEntity> items = new ArrayList<>();

  // Timestamps
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "placed_at")
  @Nullable
  private OffsetDateTime placedAt;

  @Column(name = "confirmed_at")
  @Nullable
  private OffsetDateTime confirmedAt;

  @Column(name = "fulfilled_at")
  @Nullable
  private OffsetDateTime fulfilledAt;

  @Column(name = "cancelled_at")
  @Nullable
  private OffsetDateTime cancelledAt;

  public void addItem(OrderItemEntity item) {
    items.add(item);
    item.setOrder(this);
  }

  public void removeItem(OrderItemEntity item) {
    items.remove(item);
    item.setOrder(null);
  }

  public boolean isNew() {
    return id == null;
  }

  // Getters and setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public ActorEntity getCustomer() {
    return customer;
  }

  public void setCustomer(ActorEntity customer) {
    this.customer = customer;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public BigDecimal getSubtotalAmount() {
    return subtotalAmount;
  }

  public void setSubtotalAmount(BigDecimal subtotalAmount) {
    this.subtotalAmount = subtotalAmount;
  }

  public String getSubtotalCurrency() {
    return subtotalCurrency;
  }

  public void setSubtotalCurrency(String subtotalCurrency) {
    this.subtotalCurrency = subtotalCurrency;
  }

  public BigDecimal getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(BigDecimal taxAmount) {
    this.taxAmount = taxAmount;
  }

  public String getTaxCurrency() {
    return taxCurrency;
  }

  public void setTaxCurrency(String taxCurrency) {
    this.taxCurrency = taxCurrency;
  }

  public BigDecimal getShippingAmount() {
    return shippingAmount;
  }

  public void setShippingAmount(BigDecimal shippingAmount) {
    this.shippingAmount = shippingAmount;
  }

  public String getShippingCurrency() {
    return shippingCurrency;
  }

  public void setShippingCurrency(String shippingCurrency) {
    this.shippingCurrency = shippingCurrency;
  }

  public BigDecimal getDiscountAmount() {
    return discountAmount;
  }

  public void setDiscountAmount(BigDecimal discountAmount) {
    this.discountAmount = discountAmount;
  }

  public String getDiscountCurrency() {
    return discountCurrency;
  }

  public void setDiscountCurrency(String discountCurrency) {
    this.discountCurrency = discountCurrency;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getTotalCurrency() {
    return totalCurrency;
  }

  public void setTotalCurrency(String totalCurrency) {
    this.totalCurrency = totalCurrency;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  @Nullable
  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(@Nullable String customerName) {
    this.customerName = customerName;
  }

  @Nullable
  public String getShippingFirstName() {
    return shippingFirstName;
  }

  public void setShippingFirstName(@Nullable String shippingFirstName) {
    this.shippingFirstName = shippingFirstName;
  }

  @Nullable
  public String getShippingLastName() {
    return shippingLastName;
  }

  public void setShippingLastName(@Nullable String shippingLastName) {
    this.shippingLastName = shippingLastName;
  }

  @Nullable
  public String getShippingCompany() {
    return shippingCompany;
  }

  public void setShippingCompany(@Nullable String shippingCompany) {
    this.shippingCompany = shippingCompany;
  }

  @Nullable
  public String getShippingAddressLine1() {
    return shippingAddressLine1;
  }

  public void setShippingAddressLine1(@Nullable String shippingAddressLine1) {
    this.shippingAddressLine1 = shippingAddressLine1;
  }

  @Nullable
  public String getShippingAddressLine2() {
    return shippingAddressLine2;
  }

  public void setShippingAddressLine2(@Nullable String shippingAddressLine2) {
    this.shippingAddressLine2 = shippingAddressLine2;
  }

  @Nullable
  public String getShippingCity() {
    return shippingCity;
  }

  public void setShippingCity(@Nullable String shippingCity) {
    this.shippingCity = shippingCity;
  }

  @Nullable
  public String getShippingState() {
    return shippingState;
  }

  public void setShippingState(@Nullable String shippingState) {
    this.shippingState = shippingState;
  }

  @Nullable
  public String getShippingPostalCode() {
    return shippingPostalCode;
  }

  public void setShippingPostalCode(@Nullable String shippingPostalCode) {
    this.shippingPostalCode = shippingPostalCode;
  }

  @Nullable
  public String getShippingCountry() {
    return shippingCountry;
  }

  public void setShippingCountry(@Nullable String shippingCountry) {
    this.shippingCountry = shippingCountry;
  }

  @Nullable
  public String getShippingPhone() {
    return shippingPhone;
  }

  public void setShippingPhone(@Nullable String shippingPhone) {
    this.shippingPhone = shippingPhone;
  }

  @Nullable
  public String getBillingFirstName() {
    return billingFirstName;
  }

  public void setBillingFirstName(@Nullable String billingFirstName) {
    this.billingFirstName = billingFirstName;
  }

  @Nullable
  public String getBillingLastName() {
    return billingLastName;
  }

  public void setBillingLastName(@Nullable String billingLastName) {
    this.billingLastName = billingLastName;
  }

  @Nullable
  public String getBillingCompany() {
    return billingCompany;
  }

  public void setBillingCompany(@Nullable String billingCompany) {
    this.billingCompany = billingCompany;
  }

  @Nullable
  public String getBillingAddressLine1() {
    return billingAddressLine1;
  }

  public void setBillingAddressLine1(@Nullable String billingAddressLine1) {
    this.billingAddressLine1 = billingAddressLine1;
  }

  @Nullable
  public String getBillingAddressLine2() {
    return billingAddressLine2;
  }

  public void setBillingAddressLine2(@Nullable String billingAddressLine2) {
    this.billingAddressLine2 = billingAddressLine2;
  }

  @Nullable
  public String getBillingCity() {
    return billingCity;
  }

  public void setBillingCity(@Nullable String billingCity) {
    this.billingCity = billingCity;
  }

  @Nullable
  public String getBillingState() {
    return billingState;
  }

  public void setBillingState(@Nullable String billingState) {
    this.billingState = billingState;
  }

  @Nullable
  public String getBillingPostalCode() {
    return billingPostalCode;
  }

  public void setBillingPostalCode(@Nullable String billingPostalCode) {
    this.billingPostalCode = billingPostalCode;
  }

  @Nullable
  public String getBillingCountry() {
    return billingCountry;
  }

  public void setBillingCountry(@Nullable String billingCountry) {
    this.billingCountry = billingCountry;
  }

  @Nullable
  public String getBillingPhone() {
    return billingPhone;
  }

  public void setBillingPhone(@Nullable String billingPhone) {
    this.billingPhone = billingPhone;
  }

  @Nullable
  public String getNotes() {
    return notes;
  }

  public void setNotes(@Nullable String notes) {
    this.notes = notes;
  }

  @Nullable
  public String getCustomerNotes() {
    return customerNotes;
  }

  public void setCustomerNotes(@Nullable String customerNotes) {
    this.customerNotes = customerNotes;
  }

  @Nullable
  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(@Nullable String ipAddress) {
    this.ipAddress = ipAddress;
  }

  @Nullable
  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(@Nullable String userAgent) {
    this.userAgent = userAgent;
  }

  public List<OrderItemEntity> getItems() {
    return items;
  }

  public void setItems(List<OrderItemEntity> items) {
    this.items = items;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Nullable
  public OffsetDateTime getPlacedAt() {
    return placedAt;
  }

  public void setPlacedAt(@Nullable OffsetDateTime placedAt) {
    this.placedAt = placedAt;
  }

  @Nullable
  public OffsetDateTime getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(@Nullable OffsetDateTime confirmedAt) {
    this.confirmedAt = confirmedAt;
  }

  @Nullable
  public OffsetDateTime getFulfilledAt() {
    return fulfilledAt;
  }

  public void setFulfilledAt(@Nullable OffsetDateTime fulfilledAt) {
    this.fulfilledAt = fulfilledAt;
  }

  @Nullable
  public OffsetDateTime getCancelledAt() {
    return cancelledAt;
  }

  public void setCancelledAt(@Nullable OffsetDateTime cancelledAt) {
    this.cancelledAt = cancelledAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrderEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "OrderEntity{" +
        "id=" + id +
        ", orderNumber='" + orderNumber + '\'' +
        ", status=" + status +
        ", totalAmount=" + totalAmount +
        ", totalCurrency='" + totalCurrency + '\'' +
        '}';
  }
}
