package com.simplecommerce.discount;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

/**
 * A discount code that can be applied to orders.
 *
 * @author julius.krah
 */
@Entity(name = "Discount")
@Table(name = "discounts")
public class DiscountEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  @Nullable
  private String description;

  @Enumerated(STRING)
  @Column(name = "discount_type", nullable = false, length = 50)
  private DiscountType discountType;

  @Column(name = "value_amount", precision = 19, scale = 4)
  @Nullable
  private BigDecimal valueAmount;

  @Column(name = "value_percentage", precision = 5, scale = 2)
  @Nullable
  private BigDecimal valuePercentage;

  @Column(name = "minimum_order_amount", precision = 19, scale = 4)
  @Nullable
  private BigDecimal minimumOrderAmount;

  @Column(name = "usage_limit")
  @Nullable
  private Integer usageLimit;

  @Column(name = "usage_count", nullable = false)
  private Integer usageCount = 0;

  @Column(name = "per_customer_limit")
  @Nullable
  private Integer perCustomerLimit;

  @Column(name = "starts_at")
  @Nullable
  private OffsetDateTime startsAt;

  @Column(name = "ends_at")
  @Nullable
  private OffsetDateTime endsAt;

  @Column(nullable = false)
  private Boolean active = true;

  @Column(name = "created_by", length = 250)
  @Nullable
  private String createdBy;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  public boolean isNew() {
    return id == null;
  }

  /**
   * Check if this discount is currently valid.
   */
  public boolean isValid() {
    if (!active) {
      return false;
    }

    var now = OffsetDateTime.now();

    if (startsAt != null && now.isBefore(startsAt)) {
      return false;
    }

    if (endsAt != null && now.isAfter(endsAt)) {
      return false;
    }

    if (usageLimit != null && usageCount >= usageLimit) {
      return false;
    }

    return true;
  }

  /**
   * Calculate discount amount for an order.
   */
  public BigDecimal calculateDiscount(BigDecimal orderAmount) {
    if (minimumOrderAmount != null && orderAmount.compareTo(minimumOrderAmount) < 0) {
      return BigDecimal.ZERO;
    }

    return switch (discountType) {
      case PERCENTAGE -> orderAmount.multiply(valuePercentage)
          .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
      case FIXED_AMOUNT -> valueAmount.min(orderAmount);
      case FREE_SHIPPING -> BigDecimal.ZERO; // Handled separately
      case BUY_X_GET_Y -> BigDecimal.ZERO; // Requires item-level logic
    };
  }

  // Getters and setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public String getDescription() {
    return description;
  }

  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public DiscountType getDiscountType() {
    return discountType;
  }

  public void setDiscountType(DiscountType discountType) {
    this.discountType = discountType;
  }

  @Nullable
  public BigDecimal getValueAmount() {
    return valueAmount;
  }

  public void setValueAmount(@Nullable BigDecimal valueAmount) {
    this.valueAmount = valueAmount;
  }

  @Nullable
  public BigDecimal getValuePercentage() {
    return valuePercentage;
  }

  public void setValuePercentage(@Nullable BigDecimal valuePercentage) {
    this.valuePercentage = valuePercentage;
  }

  @Nullable
  public BigDecimal getMinimumOrderAmount() {
    return minimumOrderAmount;
  }

  public void setMinimumOrderAmount(@Nullable BigDecimal minimumOrderAmount) {
    this.minimumOrderAmount = minimumOrderAmount;
  }

  @Nullable
  public Integer getUsageLimit() {
    return usageLimit;
  }

  public void setUsageLimit(@Nullable Integer usageLimit) {
    this.usageLimit = usageLimit;
  }

  public Integer getUsageCount() {
    return usageCount;
  }

  public void setUsageCount(Integer usageCount) {
    this.usageCount = usageCount;
  }

  public void incrementUsageCount() {
    this.usageCount++;
  }

  @Nullable
  public Integer getPerCustomerLimit() {
    return perCustomerLimit;
  }

  public void setPerCustomerLimit(@Nullable Integer perCustomerLimit) {
    this.perCustomerLimit = perCustomerLimit;
  }

  @Nullable
  public OffsetDateTime getStartsAt() {
    return startsAt;
  }

  public void setStartsAt(@Nullable OffsetDateTime startsAt) {
    this.startsAt = startsAt;
  }

  @Nullable
  public OffsetDateTime getEndsAt() {
    return endsAt;
  }

  public void setEndsAt(@Nullable OffsetDateTime endsAt) {
    this.endsAt = endsAt;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  @Nullable
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(@Nullable String createdBy) {
    this.createdBy = createdBy;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DiscountEntity that)) {
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
    return "DiscountEntity{" +
        "id=" + id +
        ", code='" + code + '\'' +
        ", discountType=" + discountType +
        ", active=" + active +
        '}';
  }
}
