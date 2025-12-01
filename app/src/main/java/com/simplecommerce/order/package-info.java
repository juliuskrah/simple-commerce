/**
 * The order module handles customer orders and checkout.
 *
 * <p>This module provides functionality for:
 * <ul>
 *   <li>Converting shopping carts to orders</li>
 *   <li>Managing order lifecycle and status</li>
 *   <li>Order fulfillment tracking</li>
 *   <li>Order history and retrieval</li>
 * </ul>
 *
 * @author julius.krah
 * @since 1.0
 */

@ApplicationModule(
    displayName = "Order Management",
    allowedDependencies = {"product", "product :: *", "cart", "actor", "shared", "shared :: *", "tax", "shipping", "discount"}
)
package com.simplecommerce.order;

import org.springframework.modulith.ApplicationModule;
