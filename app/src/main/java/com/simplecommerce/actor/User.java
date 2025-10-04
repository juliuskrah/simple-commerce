package com.simplecommerce.actor;

import java.time.OffsetDateTime;

public record User(
    String id,
    String username,
    OffsetDateTime updatedAt,
    OffsetDateTime createdAt,
    OffsetDateTime lastLogin,
    String email
) implements Actor {

}
