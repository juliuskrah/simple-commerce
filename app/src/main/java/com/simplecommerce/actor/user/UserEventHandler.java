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
        // user type = staff. Staff should be assigned to groups with roles that can
        // 1. view dashboard
        // 2. view all products (basic)
      }
      case COLLABORATOR -> {
        // user type = collaborator. Collaborator should be assigned roles (not groups) that can
        // 1. view dashboard
      }
      case CUSTOMER -> {
        // user type = customer. Customer should be assigned roles that can
        // 1. Complete checkout
      }
    }
  }
}
