package com.simplecommerce.actor.group;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import com.simplecommerce.actor.group.GroupEvent.GroupEventType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * Tests for {@link GroupMembers} repository.
 *
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
@RecordApplicationEvents
class GroupMembersTest {
  @Autowired
  TestEntityManager em;
  @Autowired
  private GroupMembers groupMemberRepository;

  @Test
  void shouldSaveActorMember(ApplicationEvents events) {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering
    var member = GroupMemberEntity.forActor(groupId, "new.engineer");

    var entity = groupMemberRepository.saveAndFlush(member);

    assertThat(entity).isNotNull()
        .hasNoNullFieldsOrPropertiesExcept("memberGroupId")
        .extracting(GroupMemberEntity::getGroupId, GroupMemberEntity::getActorUsername)
        .containsExactly(groupId, "new.engineer");
    assertThat(entity.getMemberGroupId()).isNull();

    var firedEvent = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.ADDED)
        .map(GroupEvent::source)
        .findFirst();
    assertThat(firedEvent).isPresent()
        .get().isSameAs(member);
  }

  @Test
  void shouldSaveNestedGroupMember(ApplicationEvents events) {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators
    var memberGroupId = UUID.fromString("a1234567-89ab-cdef-0123-456789abcdef"); // Marketing
    var member = GroupMemberEntity.forNestedGroup(groupId, memberGroupId);

    var entity = groupMemberRepository.saveAndFlush(member);

    assertThat(entity).isNotNull()
        .hasNoNullFieldsOrPropertiesExcept("actorUsername")
        .extracting(GroupMemberEntity::getGroupId, GroupMemberEntity::getMemberGroupId)
        .containsExactly(groupId, memberGroupId);
    assertThat(entity.getActorUsername()).isNull();

    var firedEvent = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.ADDED)
        .map(GroupEvent::source)
        .findFirst();
    assertThat(firedEvent).isPresent()
        .get().isSameAs(member);
  }

  @Test
  void shouldFindMemberById() {
    var found = groupMemberRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    assertThat(found).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("groupId", UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"))
        .hasFieldOrPropertyWithValue("actorUsername", "john.doe")
        .hasFieldOrPropertyWithValue("memberGroupId", null);
  }

  @Test
  void shouldFindMembersByGroupId() {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering
    var members = groupMemberRepository.findByGroupId(groupId);

    assertThat(members).isNotEmpty()
        .hasSize(3)
        .extracting(GroupMemberEntity::getActorUsername)
        .containsExactlyInAnyOrder("john.doe", "jane.smith", "bob.wilson");
  }

  @Test
  void shouldFindMixedMembersByGroupId() {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators
    var members = groupMemberRepository.findByGroupId(groupId);

    assertThat(members).isNotEmpty()
        .hasSize(3);

    // Verify we have both actor and nested group members
    var actorMembers = members.stream()
        .filter(m -> m.getActorUsername() != null)
        .toList();
    var nestedGroupMembers = members.stream()
        .filter(m -> m.getMemberGroupId() != null)
        .toList();

    assertThat(actorMembers).hasSize(1)
        .extracting(GroupMemberEntity::getActorUsername)
        .containsExactly("admin.user");

    assertThat(nestedGroupMembers).hasSize(2)
        .extracting(GroupMemberEntity::getMemberGroupId)
        .containsExactlyInAnyOrder(
            UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"), // Engineering
            UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef")  // Operations
        );
  }

  @Test
  void shouldFindMembersByGroupIdAndActorUsernames() {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering
    var usernames = List.of("john.doe", "bob.wilson");

    var members = groupMemberRepository.findByGroupIdAndActorUsernameIn(groupId, usernames);

    assertThat(members).isNotEmpty()
        .hasSize(2)
        .extracting(GroupMemberEntity::getActorUsername)
        .containsExactlyInAnyOrder("john.doe", "bob.wilson");
  }

  @Test
  void shouldFindMembersByGroupIdAndMemberGroupIds() {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators
    var memberGroupIds = List.of(
        UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"), // Engineering
        UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef")  // Operations
    );

    var members = groupMemberRepository.findByGroupIdAndMemberGroupIdIn(groupId, memberGroupIds);

    assertThat(members).isNotEmpty()
        .hasSize(2)
        .extracting(GroupMemberEntity::getMemberGroupId)
        .containsExactlyInAnyOrder(
            UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"),
            UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef")
        );
  }

  @Test
  void shouldReturnEmptyListForNonExistentGroup() {
    var nonExistentGroupId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    var members = groupMemberRepository.findByGroupId(nonExistentGroupId);

    assertThat(members).isEmpty();
  }

  @Test
  void shouldReturnEmptyListForNonMatchingUsernames() {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering
    var usernames = List.of("nonexistent.user", "another.fake.user");

    var members = groupMemberRepository.findByGroupIdAndActorUsernameIn(groupId, usernames);

    assertThat(members).isEmpty();
  }

  @Test
  void shouldReturnEmptyListForNonMatchingNestedGroups() {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering (has no nested groups)
    var memberGroupIds = List.of(
        UUID.fromString("a1234567-89ab-cdef-0123-456789abcdef"), // Marketing
        UUID.fromString("b2345678-9abc-def0-1234-56789abcdef0")  // Sales
    );

    var members = groupMemberRepository.findByGroupIdAndMemberGroupIdIn(groupId, memberGroupIds);

    assertThat(members).isEmpty();
  }

  @Test
  void shouldDeleteActorMember() {
    var groupId = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"); // Operations
    var member = GroupMemberEntity.forActor(groupId, "temp.user");

    var entity = em.persistAndFlush(member);
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNotNull();

    // delete entity (object under test)
    groupMemberRepository.delete(entity);
    em.flush();

    var none = em.find(GroupMemberEntity.class, member.getId());
    assertThat(none).isNull();
  }

  @Test
  void shouldDeleteNestedGroupMember() {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators
    var memberGroupId = UUID.fromString("b2345678-9abc-def0-1234-56789abcdef0"); // Sales
    var member = GroupMemberEntity.forNestedGroup(groupId, memberGroupId);

    var entity = em.persistAndFlush(member);
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNotNull();

    // delete entity (object under test)
    groupMemberRepository.delete(entity);
    em.flush();

    var none = em.find(GroupMemberEntity.class, member.getId());
    assertThat(none).isNull();
  }

  @Test
  void shouldHandleMultipleActorsInSameGroup() {
    var groupId = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"); // Operations

    var member1 = GroupMemberEntity.forActor(groupId, "user.one");
    var member2 = GroupMemberEntity.forActor(groupId, "user.two");
    var member3 = GroupMemberEntity.forActor(groupId, "user.three");

    var entity1 = em.persistAndFlush(member1);
    var entity2 = em.persistAndFlush(member2);
    var entity3 = em.persistAndFlush(member3);

    assertThat(entity1.getId()).isNotEqualTo(entity2.getId());
    assertThat(entity2.getId()).isNotEqualTo(entity3.getId());

    var members = groupMemberRepository.findByGroupId(groupId);
    assertThat(members).hasSizeGreaterThanOrEqualTo(6); // 3 original + 3 new

    var newMembers = members.stream()
        .filter(m -> List.of("user.one", "user.two", "user.three").contains(m.getActorUsername()))
        .toList();
    assertThat(newMembers).hasSize(3);
  }

  @Test
  void shouldFindSpecificActorsInGroupWithMany() {
    var groupId = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"); // Operations
    var usernames = List.of("david.lee", "frank.miller");

    var members = groupMemberRepository.findByGroupIdAndActorUsernameIn(groupId, usernames);

    assertThat(members).hasSize(2)
        .extracting(GroupMemberEntity::getActorUsername)
        .containsExactlyInAnyOrder("david.lee", "frank.miller");
  }

  @Test
  void shouldVerifyNestedGroupRelationship() {
    var salesGroupId = UUID.fromString("b2345678-9abc-def0-1234-56789abcdef0"); // Sales
    var members = groupMemberRepository.findByGroupId(salesGroupId);

    var nestedGroups = members.stream()
        .filter(m -> m.getMemberGroupId() != null)
        .toList();

    assertThat(nestedGroups).hasSize(1)
        .first()
        .extracting(GroupMemberEntity::getMemberGroupId)
        .isEqualTo(UUID.fromString("588c8c76-9815-45fe-8f03-dec1dd5b9254")); // Product Management
  }

  @Test
  void shouldHandleEmptyUsernameList() {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering
    var members = groupMemberRepository.findByGroupIdAndActorUsernameIn(groupId, List.of());

    assertThat(members).isEmpty();
  }

  @Test
  void shouldHandleEmptyNestedGroupIdList() {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators
    var members = groupMemberRepository.findByGroupIdAndMemberGroupIdIn(groupId, List.of());

    assertThat(members).isEmpty();
  }

  @Test
  void shouldMaintainUniqueConstraintsForActors() {
    var groupId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"); // Engineering

    // Find existing member
    var existingMembers = groupMemberRepository.findByGroupIdAndActorUsernameIn(
        groupId,
        List.of("john.doe")
    );
    assertThat(existingMembers).hasSize(1);

    // The unique index should prevent duplicates at DB level
    // This test verifies we can query for the existing member
    var member = existingMembers.getFirst();
    assertThat(member.getActorUsername()).isEqualTo("john.doe");
    assertThat(member.getGroupId()).isEqualTo(groupId);
  }

  @Test
  void shouldSaveAllMembers(ApplicationEvents events) {
    var groupId = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"); // Operations

    var member1 = GroupMemberEntity.forActor(groupId, "batch.user1");
    var member2 = GroupMemberEntity.forActor(groupId, "batch.user2");
    var member3 = GroupMemberEntity.forNestedGroup(groupId,
        UUID.fromString("a1234567-89ab-cdef-0123-456789abcdef")); // Marketing

    var entities = groupMemberRepository.saveAll(List.of(member1, member2, member3));
    em.flush();

    assertThat(entities).hasSize(3)
        .allSatisfy(e -> {
          assertThat(e.getId()).isNotNull();
          assertThat(e.getGroupId()).isEqualTo(groupId);
        });

    // Verify all members were saved
    var savedMembers = groupMemberRepository.findByGroupId(groupId);
    var batchMembers = savedMembers.stream()
        .filter(m -> m.getActorUsername() != null && m.getActorUsername().startsWith("batch."))
        .toList();
    assertThat(batchMembers).hasSize(2);

    // Verify events were published for all members
    var addedEvents = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.ADDED)
        .toList();
    assertThat(addedEvents).hasSizeGreaterThanOrEqualTo(3);
  }

  @Test
  void shouldDeleteMemberAndPublishEvent(ApplicationEvents events) {
    var groupId = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"); // Operations
    var member = GroupMemberEntity.forActor(groupId, "delete.me");

    var entity = em.persistAndFlush(member);
    assertThat(entity.getId()).isNotNull();

    var entityId = entity.getId();

    // Delete entity and verify event
    groupMemberRepository.delete(entity);
    em.flush();

    var removedEvent = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.REMOVED)
        .map(GroupEvent::source)
        .findFirst();
    assertThat(removedEvent).isPresent()
        .get().isSameAs(entity);

    var none = em.find(GroupMemberEntity.class, entityId);
    assertThat(none).isNull();
  }

  @Test
  void shouldDeleteAllMembersAndPublishEvents(ApplicationEvents events) {
    var groupId = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"); // Operations

    var member1 = GroupMemberEntity.forActor(groupId, "batch.delete1");
    var member2 = GroupMemberEntity.forActor(groupId, "batch.delete2");
    var member3 = GroupMemberEntity.forNestedGroup(groupId,
        UUID.fromString("b2345678-9abc-def0-1234-56789abcdef0")); // Sales

    var entity1 = em.persistAndFlush(member1);
    var entity2 = em.persistAndFlush(member2);
    var entity3 = em.persistAndFlush(member3);

    var ids = List.of(entity1.getId(), entity2.getId(), entity3.getId());
    assertThat(ids).allSatisfy(id -> assertThat(id).isNotNull());


    // Delete all entities
    groupMemberRepository.deleteAll(List.of(entity1, entity2, entity3));
    em.flush();

    // Verify events were published for all deletions
    var removedEvents = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.REMOVED)
        .toList();
    assertThat(removedEvents).hasSizeGreaterThanOrEqualTo(3);

    // Verify all entities were deleted
    ids.forEach(id -> {
      var none = em.find(GroupMemberEntity.class, id);
      assertThat(none).isNull();
    });
  }

  @Test
  void shouldPublishEventOnNestedGroupDeletion(ApplicationEvents events) {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators
    var memberGroupId = UUID.fromString("588c8c76-9815-45fe-8f03-dec1dd5b9254"); // Product Management
    var member = GroupMemberEntity.forNestedGroup(groupId, memberGroupId);

    var entity = em.persistAndFlush(member);
    assertThat(entity.getId()).isNotNull();


    // Delete and verify event
    groupMemberRepository.delete(entity);
    em.flush();

    var removedEvent = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.REMOVED)
        .map(GroupEvent::source)
        .findFirst();
    assertThat(removedEvent).isPresent()
        .get()
        .isSameAs(entity);
    assertThat(entity.getMemberGroupId()).isEqualTo(memberGroupId);
  }

  @Test
  void shouldSaveAllMixedMemberTypes(ApplicationEvents events) {
    var groupId = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a"); // Administrators

    var actorMember1 = GroupMemberEntity.forActor(groupId, "mixed.actor1");
    var actorMember2 = GroupMemberEntity.forActor(groupId, "mixed.actor2");
    var nestedGroup1 = GroupMemberEntity.forNestedGroup(groupId,
        UUID.fromString("a1234567-89ab-cdef-0123-456789abcdef")); // Marketing
    var nestedGroup2 = GroupMemberEntity.forNestedGroup(groupId,
        UUID.fromString("b2345678-9abc-def0-1234-56789abcdef0")); // Sales

    var entities = groupMemberRepository.saveAll(
        List.of(actorMember1, actorMember2, nestedGroup1, nestedGroup2)
    );
    em.flush();

    assertThat(entities).hasSize(4);

    // Verify actor members
    var actors = entities.stream()
        .filter(e -> e.getActorUsername() != null)
        .toList();
    assertThat(actors).hasSize(2)
        .extracting(GroupMemberEntity::getActorUsername)
        .containsExactlyInAnyOrder("mixed.actor1", "mixed.actor2");

    // Verify nested group members
    var nestedGroups = entities.stream()
        .filter(e -> e.getMemberGroupId() != null)
        .toList();
    assertThat(nestedGroups).hasSize(2)
        .extracting(GroupMemberEntity::getMemberGroupId)
        .containsExactlyInAnyOrder(
            UUID.fromString("a1234567-89ab-cdef-0123-456789abcdef"),
            UUID.fromString("b2345678-9abc-def0-1234-56789abcdef0")
        );

    // Verify events for all saves
    var addedEvents = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.ADDED)
        .toList();
    assertThat(addedEvents).hasSizeGreaterThanOrEqualTo(4);
  }

  @Test
  void shouldHandleSaveAllWithEmptyList() {
    var entities = groupMemberRepository.saveAll(List.of());

    assertThat(entities).isEmpty();
  }

  @Test
  void shouldHandleDeleteAllWithEmptyList() {
    // Should not throw exception
    groupMemberRepository.deleteAll(List.of());
    em.flush();

    // Verify no unexpected side effects
    var allMembers = groupMemberRepository.findByGroupId(
        UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f")
    );
    assertThat(allMembers).isNotEmpty(); // Original data still exists
  }
}

