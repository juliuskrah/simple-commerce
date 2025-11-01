package com.simplecommerce.actor.user;

import static com.simplecommerce.shared.types.Types.NODE_USER;

import com.simplecommerce.actor.User;
import com.simplecommerce.shared.GlobalId;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

/// @author julius.krah
@Controller
class UserController {

  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  private final ObjectProvider<UserService> userService;
  // Defer creation of the ActorService to avoid early initialization of aspectj proxy
  private final Supplier<UserService> userServiceSupplier = StableValue.supplier(UserManagement::new);

  UserController(ObjectProvider<UserService> userService) {
    this.userService = userService;
  }

  @SchemaMapping(typeName = "User")
  String id(User source) {
    return new GlobalId(NODE_USER, source.id()).encode();
  }

  @QueryMapping
  User me(Principal principal) {
    return userService.getIfAvailable(userServiceSupplier).findUser(principal.getName());
  }

  @MutationMapping
  Optional<User> addUser(@Valid @Argument CreateUserInput input) {
    LOG.debug("Attempting to add new user. User details: {}", input);
    return userService.getIfAvailable(userServiceSupplier).createUser(input);
  }
}
