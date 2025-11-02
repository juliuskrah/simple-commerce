package com.simplecommerce.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents either an actor username or a nested group membership for a given group.
 */
@Entity(name = "GroupMember")
@Table(name = "group_members", indexes = {
    @Index(name = "idx_group_members_group_id", columnList = "groupId")
})
class GroupMemberEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private UUID groupId;

  // One of actorUsername or memberGroupId must be set
  private String actorUsername;
  private UUID memberGroupId;

  protected GroupMemberEntity() {}

  private GroupMemberEntity(UUID groupId) {
    this.groupId = groupId;
  }

  static GroupMemberEntity forActor(UUID groupId, String actorUsername) {
    var m = new GroupMemberEntity(groupId);
    m.actorUsername = actorUsername;
    return m;
  }

  static GroupMemberEntity forNestedGroup(UUID groupId, UUID memberGroupId) {
    var m = new GroupMemberEntity(groupId);
    m.memberGroupId = memberGroupId;
    return m;
  }

  public UUID getId() { return id; }
  public UUID getGroupId() { return groupId; }
  public String getActorUsername() { return actorUsername; }
  public UUID getMemberGroupId() { return memberGroupId; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GroupMemberEntity that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() { return Objects.hashCode(id); }
}
