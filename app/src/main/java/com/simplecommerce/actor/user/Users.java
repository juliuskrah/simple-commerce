package com.simplecommerce.actor.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Repository for managing UserEntity instances.
 *
 * @author julius.krah
 * @since 1.0
 */
public interface Users extends Repository<UserEntity, UUID> {

  Optional<UserEntity> findById(UUID id);

  Optional<UserEntity> findByUsername(String username);

  UserEntity saveAndFlush(UserEntity user);
}
