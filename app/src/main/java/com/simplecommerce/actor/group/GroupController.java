package com.simplecommerce.actor.group;

import static com.simplecommerce.shared.types.Types.NODE_GROUP;

import com.simplecommerce.actor.Group;
import com.simplecommerce.actor.GroupMember;
import com.simplecommerce.actor.Role;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BuiltIns;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import com.simplecommerce.shared.types.GroupMemberInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

/**
 * GraphQL controller for group management (phase 0-1).
 */
@Controller
class GroupController {
  private final ObjectProvider<GroupService> groupService;
  // Defer creation of the GroupService to avoid early initialization of aspectj proxy
  private final Supplier<GroupService> groupServiceSupplier = StableValue.supplier(GroupManagement::new);
  private final KetoAuthorizationService ketoService;

  GroupController(ObjectProvider<GroupService> groupService, ObjectProvider<KetoAuthorizationService> ketoService) {
    this.groupService = groupService;
    this.ketoService = ketoService.getIfAvailable();
  }

  @SchemaMapping(typeName = "Group")
  String id(Group source) {
    return new GlobalId(NODE_GROUP, source.id()).encode();
  }

  @QueryMapping
  Optional<Group> group(@Argument String id) {
    return groupService.getIfAvailable(groupServiceSupplier).findGroup(id);
  }

  @QueryMapping
  Window<Group> groups(ScrollSubrange subrange, Sort sort) {
    var limit = subrange.count().orElse(100);
    var scroll = subrange.position().orElse(ScrollPosition.keyset());
    return groupService.getIfAvailable(groupServiceSupplier).findGroups(limit, sort, scroll);
  }

  @MutationMapping
  Group addGroup(@Argument String name, @Argument @Nullable String description) {
    return groupService.getIfAvailable(groupServiceSupplier).addGroup(name, description);
  }

  @MutationMapping
  List<? extends GroupMember> addMembersToGroup(@Argument String groupId, @Argument GroupMemberInput members) {
    var actors = members.actors() == null ? Collections.<String>emptyList() : members.actors();
    var groups = members.groups() == null ? Collections.<String>emptyList() : members.groups();
    return groupService.getIfAvailable(groupServiceSupplier).addMembers(groupId, actors, groups);
  }

  @MutationMapping
  List<? extends GroupMember> removeMembersFromGroup(@Argument String groupId, @Argument GroupMemberInput members) {
    var actors = members.actors() == null ? Collections.<String>emptyList() : members.actors();
    var groups = members.groups() == null ? Collections.<String>emptyList() : members.groups();
    return groupService.getIfAvailable(groupServiceSupplier).removeMembers(groupId, actors, groups);
  }

  @SchemaMapping(typeName = "Group")
  List<Role> roles(Group source) {
    if (ketoService == null) {
      return List.of();
    }
    // Check each built-in role assignment via Keto: Role:<name>#assignees includes Group:<gid>#members
    var gid = source.id();
    return Arrays.stream(BuiltIns.DEFAULT_ROLES)
        .filter(r -> ketoService.checkPermission("Role", r.getName(), "assignees", gid))
        .map(r -> new Role(r.getName(), null, Arrays.asList(r.getPermissions())))
        .toList();
  }
}

