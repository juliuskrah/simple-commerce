package com.simplecommerce.actor;

import com.simplecommerce.actor.ActorEvent.ActorEventType;
import com.simplecommerce.shared.DomainEvent;
import java.util.List;

public record ActorEvent<T>(T source, List<String> roles, ActorEventType eventType) implements DomainEvent<T, ActorEventType> {

  enum ActorEventType {
    GROUP_ROLE_ASSIGNED,
    USER_ROLE_ASSIGNED,
  }
}
