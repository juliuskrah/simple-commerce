package com.simplecommerce.group;

import static com.simplecommerce.shared.types.Types.NODE_GROUP;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.Role;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL controller for group management (phase 0-1).
 */
@Controller
class GroupController {
  private final ObjectProvider<GroupService> groupService;
  // Defer creation of the GroupService to avoid early initialization of aspectj proxy
  private final Supplier<GroupService> groupServiceSupplier = StableValue.supplier(GroupManagement::new);

  GroupController(ObjectProvider<GroupService> groupService) {
    this.groupService = groupService;
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
  List<Group> groups(@Argument int limit) {
    return groupService.getIfAvailable(groupServiceSupplier).findGroups(limit);
  }

  @MutationMapping
  Group addGroup(@Argument String name, @Argument @Nullable String description) {
    return groupService.getIfAvailable(groupServiceSupplier).addGroup(name, description);
  }

  @MutationMapping
  Group assignMembersToGroup(@Argument String groupId, @Argument AddGroupMembersInput input) {
    var actors = input.subject().actors() == null ? Collections.<String>emptyList() : input.subject().actors();
    var groups = input.subject().groups() == null ? Collections.<String>emptyList() : input.subject().groups();
    return groupService.getIfAvailable(groupServiceSupplier).addMembers(groupId, actors, groups);
  }

  @MutationMapping
  Group revokeMembersFromGroup(@Argument String groupId, @Argument AddGroupMembersInput input) {
    var actors = input.subject().actors() == null ? Collections.<String>emptyList() : input.subject().actors();
    var groups = input.subject().groups() == null ? Collections.<String>emptyList() : input.subject().groups();
    return groupService.getIfAvailable(groupServiceSupplier).removeMembers(groupId, actors, groups);
  }

  @MutationMapping
  Group assignProductPermissionsToGroup(@Argument AssignGroupProductPermissionsInput input) {
    return groupService.getIfAvailable(groupServiceSupplier).assignGroupProductPermissions(input.groupId(), input.productIds(), input.permission());
  }

  @MutationMapping
  Group revokeProductPermissionsFromGroup(@Argument AssignGroupProductPermissionsInput input) {
    return groupService.getIfAvailable(groupServiceSupplier).revokeGroupProductPermissions(input.groupId(), input.productIds(), input.permission());
  }

  @SchemaMapping(typeName = "Group")
  List<Role> roles(Group source) {
    // Phase 3 placeholder: resolve roles via Keto by listing Role:<name>#assignees@(Group:<gid>#members)
    // For now return empty list until query implementation is added.
    return List.of();
  }
}

record AddGroupMembersInput(SubjectGroupInput subject) {}

record SubjectGroupInput(@Nullable List<String> actors,
                         @Nullable List<String> groups) {}

record AssignGroupProductPermissionsInput(String groupId, List<String> productIds, BasePermissions permission) {}
