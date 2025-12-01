package com.simplecommerce.discount;

import com.simplecommerce.shared.types.Money;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

/**
 * A discount record for GraphQL API.
 *
 * @author julius.krah
 */
public record Discount(
    String code,
    String name,
    @Nullable String description,
    DiscountType discountType,
    @Nullable BigDecimal valuePercentage,
    @Nullable Money valueAmount,
    @Nullable Money minimumOrderAmount,
    int usageCount,
    @Nullable Integer usageLimit,
    @Nullable Integer perCustomerLimit,
    @Nullable OffsetDateTime startsAt,
    @Nullable OffsetDateTime endsAt,
    boolean active
) {
}
