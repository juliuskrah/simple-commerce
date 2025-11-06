package com.simplecommerce.shared.types;

import org.jspecify.annotations.Nullable;

/// @param actor username of actor
/// @param group ID of group
/// @author julius.krah
public record RoleAssigneeInput(@Nullable String actor, @Nullable String group) {

}
