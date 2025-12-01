/**
 * The shipping module handles shipping zones, methods, and cost calculation.
 *
 * @author julius.krah
 * @since 1.0
 */
@ApplicationModule(
    displayName = "Shipping Management",
    allowedDependencies = {"shared", "shared :: *"}
)
package com.simplecommerce.shipping;

import org.springframework.modulith.ApplicationModule;
