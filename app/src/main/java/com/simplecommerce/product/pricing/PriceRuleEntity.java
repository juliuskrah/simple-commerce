package com.simplecommerce.product.pricing;

import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.shared.utils.MonetaryUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Auditable;

/**
 * Represents a specific pricing rule within a price set.
 * Each rule defines pricing for specific contexts like geography,
 * customer groups, time periods, or quantity tiers.
 *
 * @author julius.krah
 * @since 1.0
 */
@Entity(name = "PriceRule")
@Table(name = "price_rules")
public class PriceRuleEntity implements Auditable<String, UUID, OffsetDateTime> {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  private PriceSetEntity priceSet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PriceContextType contextType;

  @Column(name = "context_value")
  private String contextValue;

  @Column(name = "price_amount", nullable = false)
  private BigDecimal priceAmount;

  @Column(name = "price_currency", nullable = false)
  private String priceCurrency;

  @Column(name = "min_quantity")
  private Integer minQuantity;

  @Column(name = "max_quantity")
  private Integer maxQuantity;

  @Column(name = "valid_from")
  private OffsetDateTime validFrom;

  @Column(name = "valid_until")
  private OffsetDateTime validUntil;

  @Column(nullable = false)
  private Boolean active = true;

  @CreationTimestamp
  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private String createdBy;
  private String updatedBy;

  @Override
  public boolean isNew() {
    return null == id;
  }

  @Override
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @Override
  public Optional<String> getCreatedBy() {
    return Optional.ofNullable(createdBy);
  }

  @Override
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public Optional<String> getLastModifiedBy() {
    return Optional.ofNullable(updatedBy);
  }

  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.updatedBy = lastModifiedBy;
  }

  @Override
  public Optional<OffsetDateTime> getCreatedDate() {
    return Optional.ofNullable(createdAt);
  }

  @Override
  public void setCreatedDate(OffsetDateTime creationDate) {
    this.createdAt = creationDate;
  }

  @Override
  public Optional<OffsetDateTime> getLastModifiedDate() {
    return Optional.ofNullable(updatedAt);
  }

  @Override
  public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
    this.updatedAt = lastModifiedDate;
  }

  public PriceSetEntity getPriceSet() {
    return priceSet;
  }

  public void setPriceSet(PriceSetEntity priceSet) {
    this.priceSet = priceSet;
  }

  public PriceContextType getContextType() {
    return contextType;
  }

  public void setContextType(PriceContextType contextType) {
    this.contextType = contextType;
  }

  public String getContextValue() {
    return contextValue;
  }

  public void setContextValue(String contextValue) {
    this.contextValue = contextValue;
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

  public Integer getMinQuantity() {
    return minQuantity;
  }

  public void setMinQuantity(Integer minQuantity) {
    this.minQuantity = minQuantity;
  }

  public Integer getMaxQuantity() {
    return maxQuantity;
  }

  public void setMaxQuantity(Integer maxQuantity) {
    this.maxQuantity = maxQuantity;
  }

  public OffsetDateTime getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(OffsetDateTime validFrom) {
    this.validFrom = validFrom;
  }

  public OffsetDateTime getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(OffsetDateTime validUntil) {
    this.validUntil = validUntil;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  /**
   * Checks if this price rule is valid for the given context and time.
   *
   * @param context the pricing context to validate against
   * @param quantity the quantity to check (optional)
   * @param currentTime the current time to validate against
   * @return true if the rule applies to the given context
   */
  public boolean isValidFor(PriceContext context, Integer quantity, OffsetDateTime currentTime) {
    if (Boolean.FALSE.equals(active)) {
      return false;
    }

    // Check time validity
    if (validFrom != null && currentTime.isBefore(validFrom)) {
      return false;
    }
    if (validUntil != null && currentTime.isAfter(validUntil)) {
      return false;
    }

    // Check quantity range
    if (quantity != null) {
      if (minQuantity != null && quantity < minQuantity) {
        return false;
      }
      if (maxQuantity != null && quantity > maxQuantity) {
        return false;
      }
    }

    // Check context type and value
    return switch (contextType) {
      case GEOGRAPHIC -> context.region() != null && context.region().equals(contextValue);
      case CURRENCY -> context.currency() != null && context.currency()
          .compareTo(MonetaryUtils.getCurrency(contextValue, context.locale())) == 0;
      case CUSTOMER_GROUP -> context.customerGroup() != null && context.customerGroup().equals(contextValue);
      case DEFAULT -> contextValue == null; // Default rule has no specific context
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PriceRuleEntity that)) {
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
    return "PriceRuleEntity{" +
        "id=" + id +
        ", contextType=" + contextType +
        ", contextValue='" + contextValue + '\'' +
        ", priceAmount=" + priceAmount +
        ", priceCurrency='" + priceCurrency + '\'' +
        ", minQuantity=" + minQuantity +
        ", maxQuantity=" + maxQuantity +
        ", validFrom=" + validFrom +
        ", validUntil=" + validUntil +
        ", active=" + active +
        '}';
  }
}