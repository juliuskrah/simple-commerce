package com.simplecommerce.order;

import org.jspecify.annotations.Nullable;

/**
 * Input for checkout process.
 *
 * @author julius.krah
 */
public record CheckoutInput(
    @Nullable String cartId,
    String customerEmail,
    @Nullable String customerName,
    @Nullable AddressInput shippingAddress,
    @Nullable AddressInput billingAddress,
    @Nullable String shippingMethodId,
    @Nullable String paymentMethodId,
    @Nullable String discountCode,
    @Nullable String customerNotes
) {
}
