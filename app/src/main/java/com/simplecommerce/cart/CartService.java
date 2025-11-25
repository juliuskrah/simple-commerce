package com.simplecommerce.cart;

/**
 * Service for managing shopping carts.
 *
 * @author julius.krah
 */
public interface CartService {

  /**
   * Get the current user's active cart. Creates a new cart if one doesn't exist.
   *
   * @return the user's cart
   */
  Cart getOrCreateCart();

  /**
   * Get a cart by its ID.
   *
   * @param id the cart ID
   * @return the cart
   */
  Cart findCartById(String id);

  /**
   * Add an item to the cart. Creates a new cart if one doesn't exist.
   *
   * @param input the add to cart input
   * @return the updated cart
   */
  Cart addToCart(AddToCartInput input);

  /**
   * Update the quantity of an item in the cart.
   *
   * @param input the update cart item input
   * @return the updated cart
   */
  Cart updateCartItem(UpdateCartItemInput input);

  /**
   * Remove an item from the cart.
   *
   * @param cartItemId the cart item ID
   * @return the updated cart
   */
  Cart removeFromCart(String cartItemId);

  /**
   * Clear all items from the cart.
   *
   * @return the empty cart
   */
  Cart clearCart();

  /**
   * Merge a guest cart with the authenticated user's cart.
   *
   * @param guestCartId the guest cart ID
   * @return the merged cart
   */
  Cart mergeCart(String guestCartId);
}
