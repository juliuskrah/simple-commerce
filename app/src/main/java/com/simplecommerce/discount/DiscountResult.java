package com.simplecommerce.discount;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Result of discount validation and application.
 *
 * @author julius.krah
 */
public record DiscountResult(
    boolean valid,
    BigDecimal discountAmount,
    @Nullable DiscountEntity discount,
    @Nullable String errorMessage
) {

  public static DiscountResult valid(BigDecimal amount, DiscountEntity discount) {
    return new DiscountResult(true, amount, discount, null);
  }

  public static DiscountResult invalid(String errorMessage) {
    return new DiscountResult(false, BigDecimal.ZERO, null, errorMessage);
  }
}
