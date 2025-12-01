package com.simplecommerce.order;

import com.simplecommerce.actor.User;
import com.simplecommerce.shared.types.Money;
import java.time.OffsetDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * An order represents a customer's purchase.
 *
 * @author julius.krah
 */
public record Order(
    String id,
    String orderNumber,
    User customer,
    OrderStatus status,
    Money subtotal,
    Money tax,
    Money shipping,
    Money discount,
    Money total,
    String customerEmail,
    @Nullable String customerName,
    @Nullable Address shippingAddress,
    @Nullable Address billingAddress,
    @Nullable String notes,
    @Nullable String customerNotes,
    List<OrderItem> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Nullable OffsetDateTime placedAt,
    @Nullable OffsetDateTime confirmedAt,
    @Nullable OffsetDateTime fulfilledAt,
    @Nullable OffsetDateTime cancelledAt
) {
}
