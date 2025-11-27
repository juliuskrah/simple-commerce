package com.simplecommerce.product.inventory;

import com.simplecommerce.product.variant.ProductVariantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;

/**
 * Audit log entry for inventory quantity adjustments.
 *
 * @author julius.krah
 */
@Entity(name = "InventoryAdjustment")
@Table(name = "inventory_adjustments")
public class InventoryAdjustmentEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "variant_id", nullable = false)
  private ProductVariantEntity variant;

  @Column(nullable = false)
  private Integer adjustment;

  @Column(name = "previous_quantity", nullable = false)
  private Integer previousQuantity;

  @Column(name = "new_quantity", nullable = false)
  private Integer newQuantity;

  @Column(length = 255)
  @Nullable
  private String reason;

  @Column(name = "adjusted_by")
  @Nullable
  private String adjustedBy;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public boolean isNew() {
    return id == null;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ProductVariantEntity getVariant() {
    return variant;
  }

  public void setVariant(ProductVariantEntity variant) {
    this.variant = variant;
  }

  public Integer getAdjustment() {
    return adjustment;
  }

  public void setAdjustment(Integer adjustment) {
    this.adjustment = adjustment;
  }

  public Integer getPreviousQuantity() {
    return previousQuantity;
  }

  public void setPreviousQuantity(Integer previousQuantity) {
    this.previousQuantity = previousQuantity;
  }

  public Integer getNewQuantity() {
    return newQuantity;
  }

  public void setNewQuantity(Integer newQuantity) {
    this.newQuantity = newQuantity;
  }

  @Nullable
  public String getReason() {
    return reason;
  }

  public void setReason(@Nullable String reason) {
    this.reason = reason;
  }

  @Nullable
  public String getAdjustedBy() {
    return adjustedBy;
  }

  public void setAdjustedBy(@Nullable String adjustedBy) {
    this.adjustedBy = adjustedBy;
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
    if (!(o instanceof InventoryAdjustmentEntity that)) {
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
    return "InventoryAdjustmentEntity{" +
        "id=" + id +
        ", adjustment=" + adjustment +
        ", previousQuantity=" + previousQuantity +
        ", newQuantity=" + newQuantity +
        ", reason='" + reason + '\'' +
        ", adjustedBy='" + adjustedBy + '\'' +
        ", createdAt=" + createdAt +
        '}';
  }
}
