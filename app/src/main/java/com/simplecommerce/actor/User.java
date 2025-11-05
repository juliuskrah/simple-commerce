package com.simplecommerce.actor;

import com.simplecommerce.shared.types.UserType;
import java.time.OffsetDateTime;

public record User(
    String id,
    String username,
    UserType userType,
    OffsetDateTime updatedAt,
    OffsetDateTime createdAt,
    OffsetDateTime lastLogin,
    String email
) implements Actor, GroupMember, RoleAssignable {

}
