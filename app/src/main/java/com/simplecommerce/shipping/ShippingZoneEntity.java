package com.simplecommerce.shipping;

import static jakarta.persistence.CascadeType.ALL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
 * A shipping zone groups locations for shipping configuration.
 *
 * @author julius.krah
 */
@Entity(name = "ShippingZone")
@Table(name = "shipping_zones")
public class ShippingZoneEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  @Nullable
  private String description;

  @Column(nullable = false)
  private Boolean active = true;

  @OneToMany(mappedBy = "zone", cascade = ALL, orphanRemoval = true)
  private List<ShippingZoneLocationEntity> locations = new ArrayList<>();

  @OneToMany(mappedBy = "zone", cascade = ALL, orphanRemoval = true)
  private List<ShippingMethodEntity> methods = new ArrayList<>();

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

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public List<ShippingZoneLocationEntity> getLocations() {
    return locations;
  }

  public void setLocations(List<ShippingZoneLocationEntity> locations) {
    this.locations = locations;
  }

  public List<ShippingMethodEntity> getMethods() {
    return methods;
  }

  public void setMethods(List<ShippingMethodEntity> methods) {
    this.methods = methods;
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
    if (!(o instanceof ShippingZoneEntity that)) {
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
    return "ShippingZoneEntity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", active=" + active +
        '}';
  }
}
