@NullMarked
@ApplicationModule(id = "actor", displayName = "User Module", allowedDependencies = {"shared", "shared :: exceptions", "shared :: authorization", "shared :: types", "node"})
package com.simplecommerce.actor;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
