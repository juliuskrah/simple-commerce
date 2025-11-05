package com.simplecommerce.actor.user;

import com.simplecommerce.actor.ActorEvent;
import com.simplecommerce.actor.User;
import com.simplecommerce.shared.authorization.AuthorizationBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/// @author julius.krah
@Component
@Profile("keto-authz")
class UserEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UserEventHandler.class);
  private final AuthorizationBridge authorizationBridge;

  UserEventHandler(AuthorizationBridge authorizationBridge) {
    this.authorizationBridge = authorizationBridge;
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.actor.user.UserEvent.UserEventType).CREATED")
  void onUserCreateAssignDefaultRole(UserEvent event) {

    switch (event.source().getUserType()) {
      case STAFF -> {
        // user type = staff. Staff should be assigned to groups with roles that can
        // 1. view dashboard
        // 2. view all products (basic)
        LOG.debug("Staff user created");
      }
      case COLLABORATOR -> {
        // user type = collaborator. Collaborator should be assigned roles (not groups) that can
        // 1. view dashboard
        LOG.debug("Collaborator user created");
      }
      case CUSTOMER -> {
        // user type = customer. Customer should be assigned roles that can
        // 1. Complete checkout
        LOG.debug("Customer user created");
      }
    }
  }

  @EventListener(condition = "#event.eventType == T(com.simplecommerce.actor.ActorEvent.ActorEventType).USER_ROLE_ASSIGNED")
  void onRoleAssignedToUser(ActorEvent<?> event) {
    if (event.source() instanceof User user) {
      authorizationBridge.assignRolesToActor(user.username(), event.roles());
      LOG.debug("Role assigned to user: {}", user);
    }
  }
}
