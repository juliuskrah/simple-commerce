package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.BasePermissions;
import java.time.OffsetDateTime;
import java.util.List;

public record Bot(
    String id,
    String username,
    OffsetDateTime updatedAt,
    OffsetDateTime createdAt,
    String appId,
    List<BasePermissions> permissions
) implements Actor, ResourcePermissible {

}
