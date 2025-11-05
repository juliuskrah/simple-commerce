package com.simplecommerce.actor.user;

/**
 * User events on the {@link UserEntity user entity} that fires on:
 * <ul>
 *   <li>{@link UserEventType#CREATED User creation}</li>
 *   <li>{@link UserEventType#UPDATED User update}</li>
 * </ul>
 * @author julius.krah
 * @see UserEntity#publishUserCreatedEvent()
 */
public record UserEvent(UserEntity source, UserEventType eventType) {
  enum UserEventType {
    CREATED,
    UPDATED,
    DELETED
  }
}
