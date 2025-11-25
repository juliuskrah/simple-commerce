package com.simplecommerce.cart;

import org.jspecify.annotations.Nullable;

/**
 * Input for adding an item to the cart.
 *
 * @author julius.krah
 */
public record AddToCartInput(
    String variantId,
    int quantity,
    @Nullable String sessionId
) {
}
