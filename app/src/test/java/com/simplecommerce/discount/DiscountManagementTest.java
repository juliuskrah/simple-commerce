package com.simplecommerce.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link DiscountManagement}.
 *
 * @author julius.krah
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class DiscountManagementTest {

  @Mock
  private Discounts discountRepository;

  @InjectMocks
  private DiscountManagement discountService;

  @Test
  @DisplayName("Should validate percentage discount correctly")
  void shouldValidatePercentageDiscount() {
    var discount = createPercentageDiscount("SAVE20", new BigDecimal("20.00"), null, true);

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "SAVE20", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isTrue();
    assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
    assertThat(result.discount()).isEqualTo(discount);
  }

  @Test
  @DisplayName("Should validate fixed amount discount correctly")
  void shouldValidateFixedAmountDiscount() {
    var discount = createFixedAmountDiscount("SAVE10", new BigDecimal("10.00"), null, true);

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "SAVE10", null, new BigDecimal("50.00"));

    assertThat(result.valid()).isTrue();
    assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
  }

  @Test
  @DisplayName("Should cap fixed amount at order total")
  void shouldCapFixedAmountAtOrderTotal() {
    var discount = createFixedAmountDiscount("BIG50", new BigDecimal("50.00"), null, true);

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "BIG50", null, new BigDecimal("30.00"));

    assertThat(result.valid()).isTrue();
    // Should cap at $30 (order total), not give $50 discount
    assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
  }

  @Test
  @DisplayName("Should reject non-existent discount code")
  void shouldRejectNonExistentCode() {
    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.empty());

    var result = discountService.validateAndApplyDiscount(
        "INVALID", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isFalse();
    assertThat(result.errorMessage()).isEqualTo("Discount code not found");
  }

  @Test
  @DisplayName("Should reject inactive discount")
  void shouldRejectInactiveDiscount() {
    var discount = createPercentageDiscount("INACTIVE", new BigDecimal("20.00"), null, false);

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "INACTIVE", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isFalse();
    assertThat(result.errorMessage()).contains("not valid");
  }

  @Test
  @DisplayName("Should reject expired discount")
  void shouldRejectExpiredDiscount() {
    var discount = createPercentageDiscount("EXPIRED", new BigDecimal("20.00"), null, true);
    discount.setStartsAt(OffsetDateTime.now().minusDays(30));
    discount.setEndsAt(OffsetDateTime.now().minusDays(1));

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "EXPIRED", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isFalse();
    assertThat(result.errorMessage()).contains("not valid");
  }

  @Test
  @DisplayName("Should reject discount before start date")
  void shouldRejectBeforeStartDate() {
    var discount = createPercentageDiscount("FUTURE", new BigDecimal("20.00"), null, true);
    discount.setStartsAt(OffsetDateTime.now().plusDays(1));

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "FUTURE", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isFalse();
    assertThat(result.errorMessage()).contains("not valid");
  }

  @Test
  @DisplayName("Should reject discount below minimum order amount")
  void shouldRejectBelowMinimumOrder() {
    var discount = createPercentageDiscount("MIN50", new BigDecimal("10.00"),
        new BigDecimal("50.00"), true);

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "MIN50", null, new BigDecimal("30.00"));

    assertThat(result.valid()).isFalse();
    assertThat(result.errorMessage()).containsIgnoringCase("minimum");
  }

  @Test
  @DisplayName("Should accept discount meeting minimum order amount")
  void shouldAcceptMeetingMinimumOrder() {
    var discount = createPercentageDiscount("MIN50", new BigDecimal("10.00"),
        new BigDecimal("50.00"), true);

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "MIN50", null, new BigDecimal("75.00"));

    assertThat(result.valid()).isTrue();
    assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("7.50")); // 10% of 75
  }

  @Test
  @DisplayName("Should reject discount exceeding usage limit")
  void shouldRejectExceedingUsageLimit() {
    var discount = createPercentageDiscount("LIMITED", new BigDecimal("20.00"), null, true);
    discount.setUsageLimit(100);
    discount.setUsageCount(100); // Already at limit

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "LIMITED", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isFalse();
    assertThat(result.errorMessage()).contains("not valid");
  }

  @Test
  @DisplayName("Should accept discount within usage limit")
  void shouldAcceptWithinUsageLimit() {
    var discount = createPercentageDiscount("LIMITED", new BigDecimal("20.00"), null, true);
    discount.setUsageLimit(100);
    discount.setUsageCount(50); // Still has capacity

    when(discountRepository.findByCodeIgnoreCase(anyString()))
        .thenReturn(Optional.of(discount));

    var result = discountService.validateAndApplyDiscount(
        "LIMITED", null, new BigDecimal("100.00"));

    assertThat(result.valid()).isTrue();
  }

  // Helper methods

  private DiscountEntity createPercentageDiscount(
      String code, BigDecimal percentage, BigDecimal minimumOrder, boolean active) {
    var discount = new DiscountEntity();
    discount.setCode(code);
    discount.setName(code + " Discount");
    discount.setDiscountType(DiscountType.PERCENTAGE);
    discount.setValuePercentage(percentage);
    discount.setMinimumOrderAmount(minimumOrder);
    discount.setActive(active);
    discount.setUsageCount(0);
    return discount;
  }

  private DiscountEntity createFixedAmountDiscount(
      String code, BigDecimal amount, BigDecimal minimumOrder, boolean active) {
    var discount = new DiscountEntity();
    discount.setCode(code);
    discount.setName(code + " Discount");
    discount.setDiscountType(DiscountType.FIXED_AMOUNT);
    discount.setValueAmount(amount);
    discount.setMinimumOrderAmount(minimumOrder);
    discount.setActive(active);
    discount.setUsageCount(0);
    return discount;
  }
}
