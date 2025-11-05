package com.simplecommerce.actor.group;

import com.simplecommerce.actor.Group;
import com.simplecommerce.actor.GroupMember;
import com.simplecommerce.actor.user.UserManagement;
import com.simplecommerce.actor.user.UserService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.AuthorizationBridge;
import com.simplecommerce.shared.authorization.BasePermissions;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of {@link GroupService}. Phase 1: handles persistence & membership; Keto integration deferred.
 */
@Transactional(readOnly = true)
@Configurable(autowire = Autowire.BY_TYPE)
public class GroupManagement implements GroupService {

  private Groups groupRepository;
  private GroupMembers groupMembersRepository;
  @Nullable
  private AuthorizationBridge authorizationBridge; // may be null when keto-authz profile not active
  private final UserService userService = new UserManagement();
  private static final String EDITORS_RELATION = "editors";
  private static final String VIEWERS_RELATION = "viewers";
  private static final String OWNERS_RELATION = "owners";

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

  @Override
  public Window<Group> findGroups(int limit, Sort sort, ScrollPosition scroll) {
    var window = groupRepository.findBy(Limit.of(limit), sort, scroll);
    return window.map(this::toDto);
  }

  @Transactional
  @Override
  public Group addGroup(String name, @Nullable String description) {
    var entity = new GroupEntity();
    entity.setName(name);
    entity.setDescription(description);
    entity = groupRepository.saveAndFlush(entity);
    return toDto(entity);
  }

  @Transactional
  @Override
  public List<? extends GroupMember> addMembers(String groupId, @Nullable List<String> actorUsernames, @Nullable List<String> nestedGroupIds) {
    var gid = decodeRequired(groupId);
    // Basic cycle prevention for direct nesting only (deep cycle detection deferred)
    var nestedUUIDs = nestedGroupIds == null ? List.<UUID>of() : nestedGroupIds.stream().map(this::decodeRequired).toList();
    var users = actorUsernames == null ? List.<String>of() : actorUsernames;
    List<GroupMemberEntity> entities = new ArrayList<>(users.size());
    for (String username : users) {
      var entity = GroupMemberEntity.forActor(gid, username);
      entities.add(entity);
    }
    groupMembersRepository.saveAll(entities);

    List<GroupMemberEntity> nestGroupEntities = new ArrayList<>(nestedUUIDs.size());
    for (UUID nested : nestedUUIDs) {
      boolean skip = nested.equals(gid) || wouldCreateCycle(gid, nested);
      if (!skip) {
        var entity = GroupMemberEntity.forNestedGroup(gid, nested);
        nestGroupEntities.add(entity);
      }
    }
    groupMembersRepository.saveAll(nestGroupEntities);

    if (users.isEmpty()) {
      return groupRepository.findByIdIn(nestedUUIDs).stream().map(this::toDto).toList();
    } else {
      return userService.findUsers(users);
    }
  }

  @Transactional
  @Override
  public List<? extends GroupMember> removeMembers(String groupId, List<String> actorUsernames, List<String> nestedGroupIds) {
    var gid = decodeRequired(groupId);
    if (!actorUsernames.isEmpty()) {
      var actors = groupMembersRepository.findByGroupIdAndActorUsernameIn(gid, actorUsernames);
      groupMembersRepository.deleteAll(actors);
    }
    List<UUID> nestedUUIDs = null;
    if (nestedGroupIds != null && !nestedGroupIds.isEmpty()) {
      nestedUUIDs = nestedGroupIds.stream().map(this::decodeRequired).toList();
      var nested = groupMembersRepository.findByGroupIdAndMemberGroupIdIn(gid, nestedUUIDs);
      groupMembersRepository.deleteAll(nested);
    }
    if (actorUsernames.isEmpty()) {
      return groupRepository.findByIdIn(nestedUUIDs).stream().map(this::toDto).toList();
    } else {
      return userService.findUsers(actorUsernames);
    }
  }

  @Transactional
  @Override
  public Group assignGroupProductsPermission(String groupId, List<String> productIds, BasePermissions permission) {
    var gid = decodeRequired(groupId);
    if (authorizationBridge != null && !productIds.isEmpty()) {
      var relation = switch (permission) {
        case CREATE_AND_EDIT_PRODUCTS, EDIT_PRODUCT_COSTS, EDIT_PRODUCT_PRICES -> EDITORS_RELATION;
        case VIEW_PRODUCTS, VIEW_PRODUCT_COSTS, EXPORT_PRODUCTS -> VIEWERS_RELATION;
        case DELETE_PRODUCTS -> OWNERS_RELATION;
        case VIEW_DASHBOARD -> VIEWERS_RELATION; // not product-specific, fallback
      };
      authorizationBridge.assignGroupPermissionOnProducts(gid.toString(), productIds, relation);
    }
    return groupRepository.findById(gid).map(this::toDto).orElseThrow();
  }

  @Transactional
  @Override
  public Group revokeGroupProductsPermission(String groupId, List<String> productIds, BasePermissions permission) {
    var gid = decodeRequired(groupId);
    if (authorizationBridge != null && !productIds.isEmpty()) {
      var relation = switch (permission) {
        case CREATE_AND_EDIT_PRODUCTS, EDIT_PRODUCT_COSTS, EDIT_PRODUCT_PRICES -> EDITORS_RELATION;
        case VIEW_PRODUCTS, VIEW_PRODUCT_COSTS, EXPORT_PRODUCTS -> VIEWERS_RELATION;
        case DELETE_PRODUCTS -> OWNERS_RELATION;
        case VIEW_DASHBOARD -> VIEWERS_RELATION;
      };
      authorizationBridge.revokeGroupPermissionOnProducts(gid.toString(), productIds, relation);
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

  /**
   * Detect if adding candidate as nested group of parent would introduce a cycle.
   * Performs a DFS from candidate following existing nested group relationships.
   */
  private boolean wouldCreateCycle(UUID parent, UUID candidate) {
    if (parent.equals(candidate)) {
      return true;
    }
    var stack = new ArrayDeque<UUID>();
    stack.push(candidate);
    var visited = new HashSet<UUID>();
    while (!stack.isEmpty()) {
      var current = stack.pop();
      if (!visited.add(current)) {
        continue;
      }
      if (current.equals(parent)) {
        return true; // cycle detected
      }
      var members = groupMembersRepository.findByGroupId(current);
      for (var nested : members.stream()
          .map(GroupMemberEntity::getMemberGroupId)
          .filter(Objects::nonNull)
          .toList()) {
        stack.push(nested);
      }
    }
    return false;
  }
}
