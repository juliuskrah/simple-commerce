package com.simplecommerce.group;

import com.simplecommerce.group.GroupEvent.GroupEventType;
import com.simplecommerce.shared.DomainEvent;

public record GroupEvent<T>(T source, GroupEventType eventType) implements DomainEvent<T, GroupEventType> {

  enum GroupEventType {
    CREATED,
    DELETED,
    ADDED,
  }
}
