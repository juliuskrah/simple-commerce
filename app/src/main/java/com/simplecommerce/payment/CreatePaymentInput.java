package com.simplecommerce.payment;

import org.jspecify.annotations.Nullable;

/**
 * Input for creating a payment.
 *
 * @author julius.krah
 */
public record CreatePaymentInput(
    String orderId,
    PaymentMethod paymentMethod,
    String amount,
    String currency,
    @Nullable String paymentMethodToken,
    @Nullable String customerIp
) {
}
