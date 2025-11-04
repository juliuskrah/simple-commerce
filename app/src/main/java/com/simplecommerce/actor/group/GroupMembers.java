package com.simplecommerce.actor.group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Repository for group members. Stores both actor usernames and nested group relationships.
 */
public interface GroupMembers extends Repository<GroupMemberEntity, UUID> {
  GroupMemberEntity saveAndFlush(GroupMemberEntity entity);
  List<GroupMemberEntity> saveAll(Iterable<GroupMemberEntity> entities);
  void delete(GroupMemberEntity entity);
  Optional<GroupMemberEntity> findById(UUID id);
  List<GroupMemberEntity> findByGroupId(UUID groupId);
  List<GroupMemberEntity> findByGroupIdAndActorUsernameIn(UUID groupId, List<String> usernames);
  List<GroupMemberEntity> findByGroupIdAndMemberGroupIdIn(UUID groupId, List<UUID> groupIds);
}
