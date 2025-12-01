package com.simplecommerce.shipping;

import static jakarta.persistence.FetchType.LAZY;

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
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

/**
 * A shipping method with pricing and delivery estimates.
 *
 * @author julius.krah
 */
@Entity(name = "ShippingMethod")
@Table(name = "shipping_methods")
public class ShippingMethodEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "zone_id", nullable = false)
  private ShippingZoneEntity zone;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  @Nullable
  private String description;

  @Column(nullable = false, length = 100)
  private String code;

  @Column(length = 100)
  @Nullable
  private String carrier;

  @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal priceAmount;

  @Column(name = "price_currency", nullable = false, length = 3)
  private String priceCurrency;

  @Column(name = "min_order_amount", precision = 19, scale = 4)
  @Nullable
  private BigDecimal minOrderAmount;

  @Column(name = "max_order_amount", precision = 19, scale = 4)
  @Nullable
  private BigDecimal maxOrderAmount;

  @Column(name = "min_weight_grams")
  @Nullable
  private Integer minWeightGrams;

  @Column(name = "max_weight_grams")
  @Nullable
  private Integer maxWeightGrams;

  @Column(name = "min_delivery_days")
  @Nullable
  private Integer minDeliveryDays;

  @Column(name = "max_delivery_days")
  @Nullable
  private Integer maxDeliveryDays;

  @Column(nullable = false)
  private Boolean active = true;

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
   * Check if this shipping method is applicable for the given order amount.
   */
  public boolean isApplicableForAmount(BigDecimal orderAmount) {
    if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
      return false;
    }
    if (maxOrderAmount != null && orderAmount.compareTo(maxOrderAmount) > 0) {
      return false;
    }
    return true;
  }

  // Getters and setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ShippingZoneEntity getZone() {
    return zone;
  }

  public void setZone(ShippingZoneEntity zone) {
    this.zone = zone;
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

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @Nullable
  public String getCarrier() {
    return carrier;
  }

  public void setCarrier(@Nullable String carrier) {
    this.carrier = carrier;
  }

  public BigDecimal getPriceAmount() {
    return priceAmount;
  }

  public void setPriceAmount(BigDecimal priceAmount) {
    this.priceAmount = priceAmount;
  }

  public String getPriceCurrency() {
    return priceCurrency;
  }

  public void setPriceCurrency(String priceCurrency) {
    this.priceCurrency = priceCurrency;
  }

  @Nullable
  public BigDecimal getMinOrderAmount() {
    return minOrderAmount;
  }

  public void setMinOrderAmount(@Nullable BigDecimal minOrderAmount) {
    this.minOrderAmount = minOrderAmount;
  }

  @Nullable
  public BigDecimal getMaxOrderAmount() {
    return maxOrderAmount;
  }

  public void setMaxOrderAmount(@Nullable BigDecimal maxOrderAmount) {
    this.maxOrderAmount = maxOrderAmount;
  }

  @Nullable
  public Integer getMinWeightGrams() {
    return minWeightGrams;
  }

  public void setMinWeightGrams(@Nullable Integer minWeightGrams) {
    this.minWeightGrams = minWeightGrams;
  }

  @Nullable
  public Integer getMaxWeightGrams() {
    return maxWeightGrams;
  }

  public void setMaxWeightGrams(@Nullable Integer maxWeightGrams) {
    this.maxWeightGrams = maxWeightGrams;
  }

  @Nullable
  public Integer getMinDeliveryDays() {
    return minDeliveryDays;
  }

  public void setMinDeliveryDays(@Nullable Integer minDeliveryDays) {
    this.minDeliveryDays = minDeliveryDays;
  }

  @Nullable
  public Integer getMaxDeliveryDays() {
    return maxDeliveryDays;
  }

  public void setMaxDeliveryDays(@Nullable Integer maxDeliveryDays) {
    this.maxDeliveryDays = maxDeliveryDays;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
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
    if (!(o instanceof ShippingMethodEntity that)) {
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
    return "ShippingMethodEntity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", code='" + code + '\'' +
        ", priceAmount=" + priceAmount +
        '}';
  }
}
