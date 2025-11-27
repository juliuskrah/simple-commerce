/**
 * Shopping cart functionality for managing customer carts and cart items.
 *
 * <p>This package provides:
 * <ul>
 *   <li>Cart management (add, update, remove items)</li>
 *   <li>Guest and authenticated user cart support</li>
 *   <li>Cart merging when guest users authenticate</li>
 *   <li>Price calculation integration</li>
 * </ul>
 *
 * @author julius.krah
 * @since 1.0
 */
@ApplicationModule(id = "cart", displayName = "Cart Module")
@org.jspecify.annotations.NullMarked
package com.simplecommerce.cart;

import org.springframework.modulith.ApplicationModule;