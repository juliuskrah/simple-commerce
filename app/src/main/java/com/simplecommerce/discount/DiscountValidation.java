package com.simplecommerce.discount;

import com.simplecommerce.shared.types.Money;
import org.jspecify.annotations.Nullable;

/**
 * Result of discount code validation for GraphQL API.
 *
 * @author julius.krah
 */
public record DiscountValidation(
    boolean valid,
    @Nullable Money discountAmount,
    @Nullable String errorMessage,
    @Nullable Discount discount
) {
}
