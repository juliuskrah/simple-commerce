package com.simplecommerce.actor;

import com.simplecommerce.node.Node;
import org.jspecify.annotations.Nullable;

/// @author julius.krah
public record Group(String id, String name, @Nullable String description) implements Node, GroupMember, RoleAssignable, ResourcePermissible {

}
