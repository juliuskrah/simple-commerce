/**
 * The discount module handles promotional codes and discount validation.
 *
 * @author julius.krah
 * @since 1.0
 */
@ApplicationModule(
    displayName = "Discount Management",
    allowedDependencies = {"actor", "shared", "shared :: *"}
)
package com.simplecommerce.discount;

import org.springframework.modulith.ApplicationModule;
