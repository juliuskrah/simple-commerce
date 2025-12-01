package com.simplecommerce.shipping;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link ShippingZoneEntity}.
 *
 * @author julius.krah
 */
public interface ShippingZones extends JpaRepository<ShippingZoneEntity, UUID> {

  /**
   * Find all active shipping zones.
   *
   * @return list of active zones
   */
  List<ShippingZoneEntity> findByActiveTrue();
}
