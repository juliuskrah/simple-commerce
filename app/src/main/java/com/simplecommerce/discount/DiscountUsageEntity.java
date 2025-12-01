package com.simplecommerce.discount;

import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.actor.ActorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Tracks discount usage for analytics and limits.
 * Uses order_id as UUID to avoid circular dependency with order module.
 *
 * @author julius.krah
 */
@Entity(name = "DiscountUsage")
@Table(name = "discount_usages")
public class DiscountUsageEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "discount_id", nullable = false)
  private DiscountEntity discount;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private ActorEntity customer;

  @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal discountAmount;

  @Column(name = "discount_currency", nullable = false, length = 3)
  private String discountCurrency;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

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

  public DiscountEntity getDiscount() {
    return discount;
  }

  public void setDiscount(DiscountEntity discount) {
    this.discount = discount;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public void setOrderId(UUID orderId) {
    this.orderId = orderId;
  }

  public ActorEntity getCustomer() {
    return customer;
  }

  public void setCustomer(ActorEntity customer) {
    this.customer = customer;
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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DiscountUsageEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
