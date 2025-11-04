package com.simplecommerce.actor.user;

import com.simplecommerce.actor.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User findUser(String username);

  List<User> findUsers(List<String> usernames);

  Optional<User> createUser(CreateUserInput user);
}
