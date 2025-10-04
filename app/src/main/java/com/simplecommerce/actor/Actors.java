package com.simplecommerce.actor;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Marker interface for actor-related classes and services.
 *
 * @since 1.0
 * @author julius.krah
 */
interface Actors extends Repository<ActorEntity, UUID> {

  Optional<ActorEntity> findById(UUID id);

  Optional<ActorEntity> findByUsername(String username);

  <T extends ActorEntity> T saveAndFlush(T actor);
}
