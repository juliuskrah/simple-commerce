@NullMarked
@ApplicationModule(id = "actor", displayName = "User Module", allowedDependencies = {
    "group",
    "shared",
    "shared :: exceptions",
    "shared :: authorization",
    "shared :: types", "node",
    "shared :: authentication",
    "shared :: utils"
})
package com.simplecommerce.actor;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
