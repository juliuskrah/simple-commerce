package com.simplecommerce.payment;

import com.simplecommerce.shared.types.Money;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

/**
 * A payment record for GraphQL API.
 *
 * @author julius.krah
 */
public record Payment(
    String id,
    String orderId,
    PaymentMethod paymentMethod,
    String paymentProvider,
    @Nullable String transactionId,
    Money amount,
    PaymentStatus status,
    @Nullable String cardLastFour,
    @Nullable String cardBrand,
    OffsetDateTime createdAt,
    @Nullable OffsetDateTime capturedAt
) {
}
