package com.simplecommerce.cart;

/**
 * Input for updating a cart item.
 *
 * @author julius.krah
 */
public record UpdateCartItemInput(
    String cartItemId,
    int quantity
) {
}
