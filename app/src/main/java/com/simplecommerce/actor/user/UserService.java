package com.simplecommerce.actor.user;

import com.simplecommerce.actor.User;
import java.util.Optional;

public interface UserService {
  User findUser(String username);

  Optional<User> createUser(CreateUserInput user);
}
