package com.simplecommerce.actor;

import com.simplecommerce.actor.ActorEvent.ActorEventType;
import com.simplecommerce.shared.DomainEvent;
import java.util.Map;

public record ActorEvent<T>(
    T source,
    Map<String, Object> data,
    ActorEventType eventType) implements DomainEvent<T, ActorEventType> {

  enum ActorEventType {
    GROUP_ROLE_ASSIGNED,
    USER_ROLE_ASSIGNED,
    ACTOR_PRODUCT_PERMISSION_ASSIGNED,
    GROUP_PRODUCT_PERMISSION_ASSIGNED,
    ROLE_PRODUCT_PERMISSION_ASSIGNED
  }
}
