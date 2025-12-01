/**
 * The tax module handles tax calculations for orders.
 *
 * @author julius.krah
 * @since 1.0
 */
@ApplicationModule(
    displayName = "Tax Calculation",
    allowedDependencies = {"shared", "shared :: *"}
)
package com.simplecommerce.tax;

import org.springframework.modulith.ApplicationModule;
