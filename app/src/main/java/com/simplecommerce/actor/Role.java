package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.BasePermissions;
import java.util.List;
import org.jspecify.annotations.Nullable;

/// @author julius.krah
public record Role(String name, @Nullable String description, List<BasePermissions> permissions) implements ResourcePermissible {

}
