package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.AuthorizationBridge;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/// @author julius.krah
@Component
@Profile("keto-authz")
class ActorEventHandler {
  private final AuthorizationBridge authorizationBridge;

  private static final Logger LOG = LoggerFactory.getLogger(ActorEventHandler.class);

  ActorEventHandler(AuthorizationBridge authorizationBridge) {
    this.authorizationBridge = authorizationBridge;
  }

  @EventListener(condition = "#event.eventType == T(com.simplecommerce.actor.ActorEvent.ActorEventType).GROUP_PRODUCT_PERMISSION_ASSIGNED")
  void onProductPermissionAssignedToGroup(ActorEvent<?> event) {
    if (event.source() instanceof Group group) {
      Map<String, Object> data = event.data();
      String relation = (String) data.get("relation");
      @SuppressWarnings("unchecked")
      List<String> productIds = (List<String>) data.get("productIds");
      authorizationBridge.assignGroupPermissionOnProducts(group.id(), productIds, relation);
      LOG.debug("{} relation on product: {} assigned to group: {}", relation, productIds, group);
    }
  }

  @EventListener(condition = "#event.eventType == T(com.simplecommerce.actor.ActorEvent.ActorEventType).ACTOR_PRODUCT_PERMISSION_ASSIGNED")
  void onProductPermissionAssignedToActor(ActorEvent<?> event) {
    if (event.source() instanceof Actor actor) {
      Map<String, Object> data = event.data();
      String relation = (String) data.get("relation");
      @SuppressWarnings("unchecked")
      List<String> productIds = (List<String>) data.get("productIds");
      authorizationBridge.assignActorPermissionOnProducts(actor.username(), productIds, relation);
      LOG.debug("{} relation on product: {} assigned to actor: {}", relation, productIds, actor);
    }
  }

  @EventListener(condition = "#event.eventType == T(com.simplecommerce.actor.ActorEvent.ActorEventType).ROLE_PRODUCT_PERMISSION_ASSIGNED")
  void onProductPermissionAssignedToRole(ActorEvent<?> event) {
    if (event.source() instanceof Role role) {
      Map<String, Object> data = event.data();
      String relation = (String) data.get("relation");
      @SuppressWarnings("unchecked")
      List<String> productIds = (List<String>) data.get("productIds");
      authorizationBridge.assignRolePermissionOnProducts(role.name(), productIds, relation);
      LOG.debug("{} relation on products: {} assigned to role: {}", relation, productIds, role);
    }
  }
}
