package com.simplecommerce.cart;

import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.product.variant.ProductVariantEntity;
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
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

/**
 * An item in a shopping cart.
 *
 * @author julius.krah
 */
@Entity(name = "CartItem")
@Table(name = "cart_items")
public class CartItemEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false)
  private CartEntity cart;

  @ManyToOne(fetch = LAZY, optional = false)
  @JoinColumn(name = "variant_id", nullable = false)
  private ProductVariantEntity variant;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal unitPriceAmount;

  @Column(name = "unit_price_currency", nullable = false, length = 3)
  private String unitPriceCurrency;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  /**
   * Gets the unit price as a MonetaryAmount.
   *
   * @return the unit price
   */
  public MonetaryAmount getUnitPrice() {
    CurrencyUnit currency = Monetary.getCurrency(unitPriceCurrency);
    return Monetary.getDefaultAmountFactory()
        .setCurrency(currency)
        .setNumber(unitPriceAmount)
        .create();
  }

  /**
   * Sets the unit price from a MonetaryAmount.
   *
   * @param price the unit price
   */
  public void setUnitPrice(MonetaryAmount price) {
    this.unitPriceAmount = price.getNumber().numberValue(BigDecimal.class);
    this.unitPriceCurrency = price.getCurrency().getCurrencyCode();
  }

  /**
   * Calculates the total price for this line item (unitPrice * quantity).
   *
   * @return the total price
   */
  public MonetaryAmount getTotalPrice() {
    return getUnitPrice().multiply(quantity);
  }

  public boolean isNew() {
    return id == null;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public CartEntity getCart() {
    return cart;
  }

  public void setCart(@Nullable CartEntity cart) {
    this.cart = cart;
  }

  public ProductVariantEntity getVariant() {
    return variant;
  }

  public void setVariant(ProductVariantEntity variant) {
    this.variant = variant;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
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
    if (!(o instanceof CartItemEntity that)) {
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
    return "CartItemEntity{" +
        "id=" + id +
        ", quantity=" + quantity +
        ", unitPriceAmount=" + unitPriceAmount +
        ", unitPriceCurrency='" + unitPriceCurrency + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
