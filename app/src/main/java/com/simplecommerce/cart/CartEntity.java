package com.simplecommerce.cart;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.actor.ActorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

/**
 * A shopping cart containing items that a customer intends to purchase.
 *
 * @author julius.krah
 */
@Entity(name = "Cart")
@Table(name = "carts")
public class CartEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "customer_id")
  @Nullable
  private ActorEntity customer;

  @Column(name = "session_id")
  @Nullable
  private String sessionId;

  @OneToMany(mappedBy = "cart", cascade = ALL, orphanRemoval = true)
  private List<CartItemEntity> items = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "expires_at")
  @Nullable
  private OffsetDateTime expiresAt;

  /**
   * Adds an item to the cart. If the variant already exists in the cart, updates the quantity.
   *
   * @param item the cart item to add
   */
  public void addItem(CartItemEntity item) {
    // Check if item with same variant already exists
    var existingItem = items.stream()
        .filter(i -> i.getVariant().getId().equals(item.getVariant().getId()))
        .findFirst();

    if (existingItem.isPresent()) {
      // Update quantity of existing item
      var existing = existingItem.get();
      existing.setQuantity(existing.getQuantity() + item.getQuantity());
    } else {
      // Add new item
      items.add(item);
      item.setCart(this);
    }
  }

  /**
   * Removes an item from the cart.
   *
   * @param item the cart item to remove
   */
  public void removeItem(CartItemEntity item) {
    items.remove(item);
    item.setCart(null);
  }

  /**
   * Clears all items from the cart.
   */
  public void clearItems() {
    items.clear();
  }

  /**
   * Gets the total quantity of items in the cart.
   *
   * @return the total quantity
   */
  public int getTotalQuantity() {
    return items.stream()
        .mapToInt(CartItemEntity::getQuantity)
        .sum();
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

  @Nullable
  public ActorEntity getCustomer() {
    return customer;
  }

  public void setCustomer(@Nullable ActorEntity customer) {
    this.customer = customer;
  }

  @Nullable
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(@Nullable String sessionId) {
    this.sessionId = sessionId;
  }

  public List<CartItemEntity> getItems() {
    return items;
  }

  public void setItems(List<CartItemEntity> items) {
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
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(@Nullable OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CartEntity that)) {
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
    return "CartEntity{" +
        "id=" + id +
        ", sessionId='" + sessionId + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", expiresAt=" + expiresAt +
        '}';
  }
}
