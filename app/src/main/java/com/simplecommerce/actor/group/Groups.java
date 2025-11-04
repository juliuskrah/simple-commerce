package com.simplecommerce.actor.group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.repository.Repository;

/**
 * Repository for {@link GroupEntity} instances.
 */
interface Groups extends Repository<GroupEntity, UUID> {
  Optional<GroupEntity> findById(UUID id);
  List<GroupEntity> findByIdIn(List<UUID> ids);
  Optional<GroupEntity> findByName(String name);
  GroupEntity saveAndFlush(GroupEntity group);
  Window<GroupEntity> findBy(Limit limit, Sort sort, ScrollPosition scroll);
  List<GroupEntity> findBy(Limit limit);
  void deleteById(UUID id);
}
