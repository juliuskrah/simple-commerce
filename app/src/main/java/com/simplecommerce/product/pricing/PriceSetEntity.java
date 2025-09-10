package com.simplecommerce.product.pricing;

import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.product.variant.ProductVariantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Auditable;

/**
 * Represents a collection of pricing rules for a product variant.
 * Each price set contains multiple price rules with different contexts
 * (geographic, currency, customer groups, time-based, quantity-based).
 *
 * @author julius.krah
 * @since 1.0
 */
@Entity(name = "PriceSet")
@Table(name = "price_sets")
public class PriceSetEntity implements Auditable<String, UUID, OffsetDateTime> {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  private ProductVariantEntity variant;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer priority = 0;

  @Column(nullable = false)
  private Boolean active = true;

  @OneToMany(mappedBy = "priceSet", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PriceRuleEntity> rules = new ArrayList<>();

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

  public ProductVariantEntity getVariant() {
    return variant;
  }

  public void setVariant(ProductVariantEntity variant) {
    this.variant = variant;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public List<PriceRuleEntity> getRules() {
    return rules;
  }

  public void setRules(List<PriceRuleEntity> rules) {
    this.rules = rules;
  }

  public void addRule(PriceRuleEntity rule) {
    rules.add(rule);
    rule.setPriceSet(this);
  }

  public void removeRule(PriceRuleEntity rule) {
    rules.remove(rule);
    rule.setPriceSet(null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PriceSetEntity that)) {
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
    return "PriceSetEntity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", priority=" + priority +
        ", active=" + active +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}