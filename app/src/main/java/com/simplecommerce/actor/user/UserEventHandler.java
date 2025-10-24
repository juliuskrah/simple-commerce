package com.simplecommerce.actor.user;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/// @author julius.krah
@Component
public class UserEventHandler {

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.actor.user.UserEvent.UserEventType).CREATED")
  void assignDefaultRole(UserEvent event) {

    switch (event.source().getUserType()) {
      case STAFF -> {
        // user type = staff
      }
      case COLLABORATOR -> {
        // user type = collaborator
      }
      case CUSTOMER -> {
        // user type = customer
      }
    }
  }
}
