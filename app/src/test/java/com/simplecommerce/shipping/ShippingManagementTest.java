package com.simplecommerce.shipping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.GlobalId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

/**
 * Tests for {@link ShippingManagement}.
 *
 * @author julius.krah
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ShippingManagementTest {

  @Mock
  private ShippingMethods shippingMethodRepository;

  @Mock
  private ObjectFactory<ShippingMethods> shippingMethodRepositoryFactory;

  private ShippingManagement shippingService;

  @BeforeEach
  void setUp() {
    when(shippingMethodRepositoryFactory.getObject()).thenReturn(shippingMethodRepository);
    shippingService = new ShippingManagement();
    shippingService.setShippingMethodRepository(shippingMethodRepositoryFactory);
  }

  @Test
  @DisplayName("Should get available shipping methods for location")
  void shouldGetAvailableShippingMethods() {
    var method1 = createShippingMethod("US_STANDARD", "Standard", new BigDecimal("5.99"), null, null);
    var method2 = createShippingMethod("US_EXPRESS", "Express", new BigDecimal("14.99"), null, null);

    when(shippingMethodRepository.findByCountryAndState(anyString(), anyString()))
        .thenReturn(List.of(method1, method2));

    var methods = shippingService.getAvailableShippingMethods(
        "US", "CA", new BigDecimal("100.00"));

    assertThat(methods).hasSize(2);
    assertThat(methods.get(0).code()).isEqualTo("US_STANDARD");
    assertThat(methods.get(1).code()).isEqualTo("US_EXPRESS");
  }

  @Test
  @DisplayName("Should filter methods by minimum order amount")
  void shouldFilterByMinimumOrderAmount() {
    var standardMethod = createShippingMethod("US_STANDARD", "Standard",
        new BigDecimal("5.99"), null, null);
    var freeShipping = createShippingMethod("US_FREE", "Free Shipping",
        BigDecimal.ZERO, new BigDecimal("50.00"), null);

    when(shippingMethodRepository.findByCountryAndState(anyString(), anyString()))
        .thenReturn(List.of(standardMethod, freeShipping));

    // Order below minimum for free shipping
    var methodsBelow = shippingService.getAvailableShippingMethods(
        "US", "CA", new BigDecimal("30.00"));

    assertThat(methodsBelow).hasSize(1);
    assertThat(methodsBelow.get(0).code()).isEqualTo("US_STANDARD");

    // Order meeting minimum for free shipping
    var methodsAbove = shippingService.getAvailableShippingMethods(
        "US", "CA", new BigDecimal("60.00"));

    assertThat(methodsAbove).hasSize(2);
  }

  @Test
  @DisplayName("Should filter methods by maximum order amount")
  void shouldFilterByMaximumOrderAmount() {
    var economyMethod = createShippingMethod("US_ECONOMY", "Economy",
        new BigDecimal("3.99"), null, new BigDecimal("25.00"));
    var standardMethod = createShippingMethod("US_STANDARD", "Standard",
        new BigDecimal("5.99"), null, null);

    when(shippingMethodRepository.findByCountryAndState(anyString(), anyString()))
        .thenReturn(List.of(economyMethod, standardMethod));

    // Order below maximum for economy
    var methodsBelow = shippingService.getAvailableShippingMethods(
        "US", "CA", new BigDecimal("20.00"));

    assertThat(methodsBelow).hasSize(2);

    // Order exceeding maximum for economy
    var methodsAbove = shippingService.getAvailableShippingMethods(
        "US", "CA", new BigDecimal("30.00"));

    assertThat(methodsAbove).hasSize(1);
    assertThat(methodsAbove.get(0).code()).isEqualTo("US_STANDARD");
  }

  @Test
  @DisplayName("Should return empty list when no methods available")
  void shouldReturnEmptyListWhenNoMethods() {
    when(shippingMethodRepository.findByCountryAndState(anyString(), anyString()))
        .thenReturn(List.of());

    var methods = shippingService.getAvailableShippingMethods(
        "XX", "YY", new BigDecimal("100.00"));

    assertThat(methods).isEmpty();
  }

  @Test
  @DisplayName("Should get shipping method by ID")
  void shouldGetShippingMethodById() {
    var methodId = UUID.randomUUID();
    var globalId = new GlobalId("ShippingMethod", methodId.toString()).encode();
    var method = createShippingMethod("US_STANDARD", "Standard", new BigDecimal("5.99"), null, null);
    method.setId(methodId);

    when(shippingMethodRepository.findById(methodId))
        .thenReturn(Optional.of(method));

    var result = shippingService.getShippingMethod(globalId);

    assertThat(result).isNotNull();
    assertThat(result.code()).isEqualTo("US_STANDARD");
  }

  @Test
  @DisplayName("Should calculate shipping cost correctly")
  void shouldCalculateShippingCost() {
    var methodId = UUID.randomUUID();
    var globalId = new GlobalId("ShippingMethod", methodId.toString()).encode();
    var method = createShippingMethod("US_STANDARD", "Standard", new BigDecimal("5.99"), null, null);
    method.setId(methodId);

    when(shippingMethodRepository.findById(methodId))
        .thenReturn(Optional.of(method));

    var cost = shippingService.calculateShippingCost(globalId, new BigDecimal("100.00"));

    assertThat(cost).isEqualByComparingTo(new BigDecimal("5.99"));
  }

  @Test
  @DisplayName("Should return zero for free shipping")
  void shouldReturnZeroForFreeShipping() {
    var methodId = UUID.randomUUID();
    var globalId = new GlobalId("ShippingMethod", methodId.toString()).encode();
    var method = createShippingMethod("US_FREE", "Free Shipping", BigDecimal.ZERO, null, null);
    method.setId(methodId);

    when(shippingMethodRepository.findById(methodId))
        .thenReturn(Optional.of(method));

    var cost = shippingService.calculateShippingCost(globalId, new BigDecimal("100.00"));

    assertThat(cost).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("Should return methods in order provided by repository")
  void shouldReturnMethodsInRepositoryOrder() {
    var method1 = createShippingMethod("US_EXPRESS", "Express", new BigDecimal("24.99"), null, null);
    var method2 = createShippingMethod("US_STANDARD", "Standard", new BigDecimal("5.99"), null, null);

    // Repository already sorts by price (ORDER BY in SQL)
    when(shippingMethodRepository.findByCountryAndState(anyString(), anyString()))
        .thenReturn(List.of(method2, method1)); // Assume pre-sorted

    var methods = shippingService.getAvailableShippingMethods(
        "US", "CA", new BigDecimal("100.00"));

    assertThat(methods).hasSize(2);
    assertThat(methods.get(0).code()).isEqualTo("US_STANDARD");
    assertThat(methods.get(1).code()).isEqualTo("US_EXPRESS");
  }

  // Helper method

  private ShippingMethodEntity createShippingMethod(
      String code, String name, BigDecimal price,
      BigDecimal minOrder, BigDecimal maxOrder) {
    var zone = new ShippingZoneEntity();
    zone.setId(UUID.randomUUID());
    zone.setName("Test Zone");
    zone.setActive(true);

    var method = new ShippingMethodEntity();
    method.setId(UUID.randomUUID());
    method.setZone(zone);
    method.setCode(code);
    method.setName(name);
    method.setPriceAmount(price);
    method.setPriceCurrency("USD");
    method.setMinOrderAmount(minOrder);
    method.setMaxOrderAmount(maxOrder);
    method.setActive(true);
    return method;
  }
}
