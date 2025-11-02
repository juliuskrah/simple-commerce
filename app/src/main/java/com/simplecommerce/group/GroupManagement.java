package com.simplecommerce.group;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.authorization.AuthorizationBridge;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of {@link GroupService}. Phase 1: handles persistence & membership; Keto integration deferred.
 */
@Transactional(readOnly = true)
@Configurable(autowire = Autowire.BY_TYPE)
class GroupManagement implements GroupService {

  private Groups groupRepository;
  private GroupMembers groupMembersRepository;
  private AuthorizationBridge authorizationBridge; // may be null when keto-authz profile not active

  public void setGroupRepository(ObjectProvider<Groups> groupRepository) {
    this.groupRepository = groupRepository.getObject();
  }

  public void setGroupMembersRepository(ObjectProvider<GroupMembers> groupMembersRepository) {
    this.groupMembersRepository = groupMembersRepository.getObject();
  }

  public void setAuthorizationBridge(ObjectProvider<AuthorizationBridge> authorizationBridge) {
    this.authorizationBridge = authorizationBridge.getIfAvailable();
  }

  @Override
  public Optional<Group> findGroup(String id) {
    return decode(id).flatMap(groupRepository::findById).map(this::toDto);
  }

  @Override
  public List<Group> findGroups(int limit) {
    return groupRepository.findBy(Limit.of(limit)).stream().map(this::toDto).toList();
  }

  @Transactional
  @Override
  public Group addGroup(String name, @org.jspecify.annotations.Nullable String description) {
    var entity = new GroupEntity();
    entity.setName(name);
    entity.setDescription(description);
    entity = groupRepository.saveAndFlush(entity);
    return toDto(entity);
  }

  @Transactional
  @Override
  public Group addMembers(String groupId, @org.jspecify.annotations.Nullable List<String> actorUsernames, @org.jspecify.annotations.Nullable List<String> nestedGroupIds) {
    var gid = decodeRequired(groupId);
    // Basic cycle prevention for direct nesting only (deep cycle detection deferred)
    var nestedUUIDs = nestedGroupIds == null ? List.<UUID>of() : nestedGroupIds.stream().map(this::decodeRequired).toList();
    var users = actorUsernames == null ? List.<String>of() : actorUsernames;
    for (String username : users) {
      var entity = GroupMemberEntity.forActor(gid, username);
      groupMembersRepository.saveAndFlush(entity);
    }
    for (UUID nested : nestedUUIDs) {
      if (nested.equals(gid)) {
        continue; // prevent self-cycle
      }
      var entity = GroupMemberEntity.forNestedGroup(gid, nested);
      groupMembersRepository.saveAndFlush(entity);
    }
    if (authorizationBridge != null) {
      if (!users.isEmpty()) {
        authorizationBridge.addActorsToGroup(gid.toString(), users);
      }
      if (!nestedUUIDs.isEmpty()) {
        authorizationBridge.addGroupsToGroup(gid.toString(), nestedUUIDs.stream().map(UUID::toString).toList());
      }
    }
    return groupRepository.findById(gid).map(this::toDto).orElseThrow();
  }

  @Transactional
  @Override
  public Group removeMembers(String groupId, @org.jspecify.annotations.Nullable List<String> actorUsernames, @org.jspecify.annotations.Nullable List<String> nestedGroupIds) {
    var gid = decodeRequired(groupId);
    if (actorUsernames != null && !actorUsernames.isEmpty()) {
      var actors = groupMembersRepository.findByGroupIdAndActorUsernameIn(gid, actorUsernames);
      actors.forEach(groupMembersRepository::delete);
    }
    if (nestedGroupIds != null && !nestedGroupIds.isEmpty()) {
      var nestedUUIDs = nestedGroupIds.stream().map(this::decodeRequired).toList();
      var nested = groupMembersRepository.findByGroupIdAndMemberGroupIdIn(gid, nestedUUIDs);
      nested.forEach(groupMembersRepository::delete);
    }
    return groupRepository.findById(gid).map(this::toDto).orElseThrow();
  }

  @Transactional
  @Override
  public Group assignGroupProductPermissions(String groupId, List<String> productIds, BasePermissions permission) {
    var gid = decodeRequired(groupId);
    if (authorizationBridge != null && !productIds.isEmpty()) {
      var relation = switch (permission) {
        case CREATE_AND_EDIT_PRODUCTS, EDIT_PRODUCT_COSTS, EDIT_PRODUCT_PRICES -> "editors";
        case VIEW_PRODUCTS, VIEW_PRODUCT_COSTS, EXPORT_PRODUCTS -> "viewers";
        case DELETE_PRODUCTS -> "owners";
        case VIEW_DASHBOARD -> "viewers"; // not product-specific, fallback
      };
      authorizationBridge.assignGroupPermissionOnProducts(gid.toString(), productIds, relation);
    }
    return groupRepository.findById(gid).map(this::toDto).orElseThrow();
  }

  private Group toDto(GroupEntity entity) {
    var id = Objects.requireNonNull(entity.getId(), "Group ID not generated").toString();
    return new Group(id, entity.getName(), entity.getDescription());
  }

  private Optional<UUID> decode(String id) {
    try {
      var gid = GlobalId.decode(id);
      return Optional.of(UUID.fromString(gid.id()));
    } catch (Exception _) {
      return Optional.empty();
    }
  }

  private UUID decodeRequired(String id) {
    return decode(id).orElseThrow(() -> new IllegalArgumentException("Invalid group id " + id));
  }
}
