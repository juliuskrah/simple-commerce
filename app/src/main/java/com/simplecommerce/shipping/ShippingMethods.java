package com.simplecommerce.shipping;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link ShippingMethodEntity}.
 *
 * @author julius.krah
 */
public interface ShippingMethods extends JpaRepository<ShippingMethodEntity, UUID> {

  /**
   * Find shipping methods for a zone by country and state.
   *
   * @param country the country code
   * @param state the state code
   * @return list of applicable shipping methods
   */
  @Query("""
      SELECT DISTINCT sm FROM ShippingMethod sm
      JOIN sm.zone z
      JOIN z.locations l
      WHERE z.active = true
        AND sm.active = true
        AND l.countryCode = :country
        AND (l.stateCode IS NULL OR l.stateCode = :state)
      ORDER BY sm.priceAmount ASC
      """)
  List<ShippingMethodEntity> findByCountryAndState(
      @Param("country") String country,
      @Param("state") String state
  );

  /**
   * Find all active shipping methods for a zone.
   *
   * @param zoneId the zone ID
   * @return list of shipping methods
   */
  @Query("SELECT sm FROM ShippingMethod sm WHERE sm.zone.id = :zoneId AND sm.active = true")
  List<ShippingMethodEntity> findByZoneIdAndActiveTrue(@Param("zoneId") UUID zoneId);
}
