package com.simplecommerce.shipping;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for shipping management.
 *
 * @author julius.krah
 */
public interface ShippingService {

  /**
   * Get available shipping methods for a destination.
   *
   * @param country the destination country code
   * @param state the destination state code
   * @param orderAmount the order subtotal amount
   * @return list of applicable shipping methods
   */
  List<ShippingMethod> getAvailableShippingMethods(
      String country,
      String state,
      BigDecimal orderAmount
  );

  /**
   * Get a shipping method by ID.
   *
   * @param id the shipping method ID
   * @return the shipping method
   */
  ShippingMethod getShippingMethod(String id);

  /**
   * Calculate shipping cost for a method.
   *
   * @param methodId the shipping method ID
   * @param orderAmount the order amount
   * @return the shipping cost
   */
  BigDecimal calculateShippingCost(String methodId, BigDecimal orderAmount);
}
