package com.simplecommerce.actor.user;

import com.simplecommerce.actor.User;

public interface UserService {
  User findUser(String username);
}
