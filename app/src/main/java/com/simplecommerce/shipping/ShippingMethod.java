package com.simplecommerce.shipping;

import com.simplecommerce.shared.types.Money;
import org.jspecify.annotations.Nullable;

/**
 * A shipping method record for GraphQL API.
 *
 * @author julius.krah
 */
public record ShippingMethod(
    String id,
    String name,
    @Nullable String description,
    String code,
    @Nullable String carrier,
    Money price,
    @Nullable Integer minDeliveryDays,
    @Nullable Integer maxDeliveryDays
) {
}
