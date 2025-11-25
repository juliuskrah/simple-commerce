package com.simplecommerce.cart;

import com.simplecommerce.actor.User;
import com.simplecommerce.shared.types.Money;
import java.time.OffsetDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A shopping cart containing items that a customer intends to purchase.
 *
 * @author julius.krah
 */
public record Cart(
    String id,
    @Nullable User customer,
    @Nullable String sessionId,
    List<CartItem> items,
    int totalQuantity,
    Money subtotal,
    @Nullable Money estimatedTax,
    Money total,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Nullable OffsetDateTime expiresAt
) {
}
