package com.simplecommerce.actor;

import static com.simplecommerce.shared.types.Types.NODE_BOT;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.RoleAssigneeInput;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

/**
 * Controller for managing actors in the system. Provides endpoints for creating, updating, deleting, and retrieving actors.
 *
 * @author julius.krah
 * @since 1.0
 */
@Controller
class ActorController {
  private final ObjectProvider<ActorService> actorService;
  // Defer creation of the ActorService to avoid early initialization of aspectj proxy
  private final Supplier<ActorService> actorServiceSupplier = StableValue.supplier(ActorManagement::new);

  ActorController(ObjectProvider<ActorService> actorService) {
    this.actorService = actorService;
  }

  @SchemaMapping(typeName = "Bot")
  String id(Bot source) {
    return new GlobalId(NODE_BOT, source.id()).encode();
  }

  @QueryMapping
  Optional<Actor> actor(@Argument String username) {
    return actorService.getIfAvailable(actorServiceSupplier).findActor(username);
  }

  @QueryMapping
  List<Role> roles() {
    return actorService.getIfAvailable(actorServiceSupplier).findRoles();
  }

  @QueryMapping
  List<BasePermissions> permissions() {
    return actorService.getIfAvailable(actorServiceSupplier).findPermissions();
  }

  @MutationMapping
  RoleAssignable addAssigneesToRoles(@Argument List<String> roles, @Argument RoleAssigneeInput assignees) {
    return actorService.getIfAvailable(actorServiceSupplier).addRolesToSubject(roles, assignees);
  }

  @MutationMapping
  RoleAssignable removeAssigneesFromRoles(@Argument List<String> roles, @Argument RoleAssigneeInput assignees) {
    return actorService.getIfAvailable(actorServiceSupplier).removeRolesFromSubject(roles, assignees);
  }

  @MutationMapping
  ResourcePermissible assignProductPermissionToSubject(@Argument ProductPermissionForSubjectInput input) {
    return actorService.getIfAvailable(actorServiceSupplier).assignProductPermission(input.subject(), input.productIds(), input.permission());
  }

  @MutationMapping
  ResourcePermissible revokeProductPermissionFromSubject(@Argument ProductPermissionForSubjectInput input) {
    return actorService.getIfAvailable(actorServiceSupplier).revokeProductPermission(input.subject(), input.productIds(), input.permission());
  }
}
