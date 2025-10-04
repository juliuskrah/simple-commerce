package com.simplecommerce.actor;

import java.util.Optional;

/**
 * Service interface for managing actors in the system. Provides methods to find, create, update, and delete actors.
 *
 * @author julius.krah
 * @since 1.0
 */
public interface ActorService {

  Optional<Actor> findActor(String username);

  User findUser(String username);
}
