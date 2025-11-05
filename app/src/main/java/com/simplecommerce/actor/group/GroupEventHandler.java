package com.simplecommerce.actor.group;

import com.simplecommerce.actor.ActorEvent;
import com.simplecommerce.actor.Group;
import com.simplecommerce.shared.authorization.AuthorizationBridge;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/// @author julius.krah
@Component
@Profile("keto-authz")
class GroupEventHandler {
  private final AuthorizationBridge authorizationBridge;

  GroupEventHandler(AuthorizationBridge authorizationBridge) {
    this.authorizationBridge = authorizationBridge;
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.actor.group.GroupEvent.GroupEventType).ADDED")
  void onAddGroupMember(GroupEvent<?> event) {
    if (event.source() instanceof GroupMemberEntity memberEntity) {
      LOG.info("Adding group member (user:{}:, group:{})", memberEntity.getActorUsername(), memberEntity.getMemberGroupId());
      var groupId = memberEntity.getGroupId().toString();
      if (memberEntity.getMemberGroupId() != null) {
        authorizationBridge.addGroupsToGroup(groupId, List.of(memberEntity.getMemberGroupId().toString()));
      } else if (memberEntity.getActorUsername() != null) {
        authorizationBridge.addActorsToGroup(groupId, List.of(memberEntity.getActorUsername()));
      }
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(GroupEventHandler.class);

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.actor.group.GroupEvent.GroupEventType).REMOVED")
  void onRemoveGroupMember(GroupEvent<?> event) {
    if (event.source() instanceof GroupMemberEntity memberEntity) {
      LOG.info("Removing group member (user:{}, group:{})", memberEntity.getActorUsername(), memberEntity.getMemberGroupId());
      var groupId = memberEntity.getGroupId().toString();
      if (memberEntity.getMemberGroupId() != null) {
        authorizationBridge.removeGroupsFromGroup(groupId, List.of(memberEntity.getMemberGroupId().toString()));
      } else if (memberEntity.getActorUsername() != null) {
        authorizationBridge.removeActorsFromGroup(groupId, List.of(memberEntity.getActorUsername()));
      }
    }
  }

  @EventListener(condition = "#event.eventType == T(com.simplecommerce.actor.ActorEvent.ActorEventType).GROUP_ROLE_ASSIGNED")
  void onRoleAssignedToGroup(ActorEvent<?> event) {
    if (event.source() instanceof Group group) {
      authorizationBridge.assignRolesToGroup(group.id(),  event.roles());
      LOG.debug("Role assigned to group: {}", group);
    }
  }
}
