package com.simplecommerce.actor;

import com.simplecommerce.shared.types.Role;
import org.jspecify.annotations.Nullable;

/// @author julius.krah
public record PermissionAssignmentPayload(@Nullable Actor actor, @Nullable Group group, @Nullable Role role) {

}
