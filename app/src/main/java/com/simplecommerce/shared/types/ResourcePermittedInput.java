package com.simplecommerce.shared.types;

import org.jspecify.annotations.Nullable;

/// @param actor username of actor
/// @param group ID of group
/// @param role name of role
/// @author julius.krah
public record ResourcePermittedInput(@Nullable String actor, @Nullable String group, @Nullable String role) {

}
