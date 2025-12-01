package com.simplecommerce.shipping;

import static jakarta.persistence.FetchType.LAZY;

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
 * A location within a shipping zone.
 *
 * @author julius.krah
 */
@Entity(name = "ShippingZoneLocation")
@Table(name = "shipping_zone_locations")
public class ShippingZoneLocationEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "zone_id", nullable = false)
  private ShippingZoneEntity zone;

  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @Column(name = "state_code", length = 100)
  @Nullable
  private String stateCode;

  @Column(name = "postal_code_pattern", length = 100)
  @Nullable
  private String postalCodePattern;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

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

  public ShippingZoneEntity getZone() {
    return zone;
  }

  public void setZone(ShippingZoneEntity zone) {
    this.zone = zone;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  @Nullable
  public String getStateCode() {
    return stateCode;
  }

  public void setStateCode(@Nullable String stateCode) {
    this.stateCode = stateCode;
  }

  @Nullable
  public String getPostalCodePattern() {
    return postalCodePattern;
  }

  public void setPostalCodePattern(@Nullable String postalCodePattern) {
    this.postalCodePattern = postalCodePattern;
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
    if (!(o instanceof ShippingZoneLocationEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
