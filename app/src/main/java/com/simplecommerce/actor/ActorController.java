package com.simplecommerce.actor;

import static com.simplecommerce.shared.types.Types.NODE_BOT;
import static com.simplecommerce.shared.types.Types.NODE_USER;

import com.simplecommerce.shared.GlobalId;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
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

  @SchemaMapping(typeName = "User")
  String id(User source) {
    return new GlobalId(NODE_USER, source.id()).encode();
  }

  @SchemaMapping(typeName = "Bot")
  String id(Bot source) {
    return new GlobalId(NODE_BOT, source.id()).encode();
  }

  @QueryMapping
  User me(Principal principal) {
    return actorService.getIfAvailable(actorServiceSupplier).findUser(principal.getName());
  }

  @QueryMapping
  Optional<Actor> actor(@Argument String username) {
    return actorService.getIfAvailable(actorServiceSupplier).findActor(username);
  }

  @MutationMapping
  Actor assignPermissionsToActor(@Argument String username, @Argument List<@NonNull PermissionTupleInput> permissions) {
    return null;
  }

  @MutationMapping
  Actor revokePermissionsFromActor(@Argument String username, @Argument List<@NonNull PermissionTupleInput> permissions) {
    return null;
  }
}
