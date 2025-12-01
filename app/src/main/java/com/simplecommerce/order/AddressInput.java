package com.simplecommerce.order;

import org.jspecify.annotations.Nullable;

/**
 * Input for address information.
 *
 * @author julius.krah
 */
public record AddressInput(
    @Nullable String firstName,
    @Nullable String lastName,
    @Nullable String company,
    String addressLine1,
    @Nullable String addressLine2,
    String city,
    @Nullable String state,
    String postalCode,
    String country,
    @Nullable String phone
) {
}
