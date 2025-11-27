package com.simplecommerce.product.variant;

import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.product.pricing.PriceSetEntity;
import com.simplecommerce.product.ProductEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
 * @author julius.krah
 */
@Entity(name = "ProductVariant")
@Table(name = "product_variants")
public class ProductVariantEntity implements Auditable<String, UUID, OffsetDateTime> {
  @Id
  @GeneratedValue
  private UUID id;
  @ManyToOne(fetch = LAZY)
  private ProductEntity product;
  @Column(unique = true, nullable = false)
  private String sku;
  private String title;
  @Column(name = "price_amount")
  private BigDecimal priceAmount;
  @Column(name = "price_currency")
  private String priceCurrency;
  @Column(name = "system_generated", nullable = false)
  private Boolean systemGenerated = true;
  @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PriceSetEntity> priceSets = new ArrayList<>();
  @Column(name = "track_inventory", nullable = false)
  private Boolean trackInventory = false;
  @Column(name = "available_quantity")
  private Integer availableQuantity;
  @Column(name = "low_stock_threshold")
  private Integer lowStockThreshold = 10;
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

  public ProductEntity getProduct() {
    return product;
  }

  public void setProduct(ProductEntity product) {
    this.product = product;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public Boolean getSystemGenerated() {
    return systemGenerated;
  }

  public void setSystemGenerated(Boolean systemGenerated) {
    this.systemGenerated = systemGenerated;
  }

  public List<PriceSetEntity> getPriceSets() {
    return priceSets;
  }

  public void setPriceSets(List<PriceSetEntity> priceSets) {
    this.priceSets = priceSets;
  }

  public void addPriceSet(PriceSetEntity priceSet) {
    priceSets.add(priceSet);
    priceSet.setVariant(this);
  }

  public void removePriceSet(PriceSetEntity priceSet) {
    priceSets.remove(priceSet);
    priceSet.setVariant(null);
  }

  public Boolean getTrackInventory() {
    return trackInventory;
  }

  public void setTrackInventory(Boolean trackInventory) {
    this.trackInventory = trackInventory;
  }

  public Integer getAvailableQuantity() {
    return availableQuantity;
  }

  public void setAvailableQuantity(Integer availableQuantity) {
    this.availableQuantity = availableQuantity;
  }

  public Integer getLowStockThreshold() {
    return lowStockThreshold;
  }

  public void setLowStockThreshold(Integer lowStockThreshold) {
    this.lowStockThreshold = lowStockThreshold;
  }

  /**
   * Check if the variant is in stock.
   * @return true if inventory tracking is disabled or available quantity > 0
   */
  public boolean isInStock() {
    if (!trackInventory) {
      return true; // Always in stock if not tracking
    }
    return availableQuantity != null && availableQuantity > 0;
  }

  /**
   * Check if the variant is low in stock.
   * @return true if tracking is enabled and quantity is below threshold
   */
  public boolean isLowStock() {
    if (!trackInventory || availableQuantity == null) {
      return false;
    }
    int threshold = lowStockThreshold != null ? lowStockThreshold : 10;
    return availableQuantity > 0 && availableQuantity <= threshold;
  }

  /**
   * Adjust the available quantity by the given amount.
   * @param adjustment the quantity to add (positive) or subtract (negative)
   * @return the new quantity
   * @throws IllegalStateException if inventory tracking is not enabled
   */
  public int adjustQuantity(int adjustment) {
    if (!trackInventory) {
      throw new IllegalStateException("Cannot adjust quantity when inventory tracking is disabled");
    }
    if (availableQuantity == null) {
      availableQuantity = 0;
    }
    int newQuantity = availableQuantity + adjustment;
    if (newQuantity < 0) {
      throw new IllegalArgumentException("Insufficient inventory: cannot reduce quantity below 0");
    }
    availableQuantity = newQuantity;
    return availableQuantity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProductVariantEntity that)) {
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
    return "ProductVariantEntity{" +
        "id=" + id +
        ", sku='" + sku + '\'' +
        ", title='" + title + '\'' +
        ", priceAmount=" + priceAmount +
        ", priceCurrency='" + priceCurrency + '\'' +
        ", systemGenerated=" + systemGenerated +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
