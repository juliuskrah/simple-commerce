package com.simplecommerce.order;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.product.variant.ProductVariantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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

/**
 * An item in an order representing a product variant purchase.
 *
 * @author julius.krah
 */
@Entity(name = "OrderItem")
@Table(name = "order_items")
public class OrderItemEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "variant_id", nullable = false)
  private ProductVariantEntity variant;

  // Product snapshot at time of order
  @Column(name = "product_title", nullable = false, length = 500)
  private String productTitle;

  @Column(name = "variant_title", nullable = false, length = 500)
  private String variantTitle;

  @Column(nullable = false, length = 100)
  private String sku;

  // Pricing
  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal unitPriceAmount;

  @Column(name = "unit_price_currency", nullable = false, length = 3)
  private String unitPriceCurrency;

  @Column(name = "total_price_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal totalPriceAmount;

  @Column(name = "total_price_currency", nullable = false, length = 3)
  private String totalPriceCurrency;

  @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "tax_currency", nullable = false, length = 3)
  private String taxCurrency;

  @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "discount_currency", nullable = false, length = 3)
  private String discountCurrency;

  // Fulfillment
  @Enumerated(STRING)
  @Column(name = "fulfillment_status", nullable = false, length = 50)
  private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.UNFULFILLED;

  // Timestamps
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

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

  public OrderEntity getOrder() {
    return order;
  }

  public void setOrder(OrderEntity order) {
    this.order = order;
  }

  public ProductVariantEntity getVariant() {
    return variant;
  }

  public void setVariant(ProductVariantEntity variant) {
    this.variant = variant;
  }

  public String getProductTitle() {
    return productTitle;
  }

  public void setProductTitle(String productTitle) {
    this.productTitle = productTitle;
  }

  public String getVariantTitle() {
    return variantTitle;
  }

  public void setVariantTitle(String variantTitle) {
    this.variantTitle = variantTitle;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getUnitPriceAmount() {
    return unitPriceAmount;
  }

  public void setUnitPriceAmount(BigDecimal unitPriceAmount) {
    this.unitPriceAmount = unitPriceAmount;
  }

  public String getUnitPriceCurrency() {
    return unitPriceCurrency;
  }

  public void setUnitPriceCurrency(String unitPriceCurrency) {
    this.unitPriceCurrency = unitPriceCurrency;
  }

  public BigDecimal getTotalPriceAmount() {
    return totalPriceAmount;
  }

  public void setTotalPriceAmount(BigDecimal totalPriceAmount) {
    this.totalPriceAmount = totalPriceAmount;
  }

  public String getTotalPriceCurrency() {
    return totalPriceCurrency;
  }

  public void setTotalPriceCurrency(String totalPriceCurrency) {
    this.totalPriceCurrency = totalPriceCurrency;
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

  public FulfillmentStatus getFulfillmentStatus() {
    return fulfillmentStatus;
  }

  public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
    this.fulfillmentStatus = fulfillmentStatus;
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
    if (!(o instanceof OrderItemEntity that)) {
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
    return "OrderItemEntity{" +
        "id=" + id +
        ", sku='" + sku + '\'' +
        ", quantity=" + quantity +
        ", totalPriceAmount=" + totalPriceAmount +
        '}';
  }
}
