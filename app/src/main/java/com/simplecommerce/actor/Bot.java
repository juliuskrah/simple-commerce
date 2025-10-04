package com.simplecommerce.actor;

import java.time.OffsetDateTime;

public record Bot(
    String id,
    String username,
    OffsetDateTime updatedAt,
    OffsetDateTime createdAt
) implements Actor {

}
