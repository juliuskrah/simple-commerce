package com.simplecommerce.group;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import com.simplecommerce.group.GroupEvent.GroupEventType;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * Tests for {@link Groups} repository.
 *
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
@RecordApplicationEvents
class GroupsTest {
  @Autowired
  TestEntityManager em;
  @Autowired
  private Groups groupRepository;

  @Test
  void shouldSaveGroup(ApplicationEvents events) {
    var group = new GroupEntity();
    group.setName("Backend Engineers");
    group.setDescription("Backend engineering team");
    var entity = groupRepository.saveAndFlush(group);
    assertThat(entity).isNotNull()
        .hasNoNullFieldsOrPropertiesExcept("createdBy", "updatedBy")
        .extracting(GroupEntity::getName, GroupEntity::getDescription)
        .containsExactly("Backend Engineers", "Backend engineering team");
    var firedEvent = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.CREATED)
        .map(GroupEvent::source)
        .findFirst();
    assertThat(firedEvent).isPresent()
        .get().isSameAs(group);
  }

  @Test
  void shouldSaveGroupWithoutDescription(ApplicationEvents events) {
    var group = new GroupEntity();
    group.setName("Frontend Engineers");
    var entity = groupRepository.saveAndFlush(group);
    assertThat(entity).isNotNull()
        .hasNoNullFieldsOrPropertiesExcept("description", "createdBy", "updatedBy")
        .extracting(GroupEntity::getName)
        .isEqualTo("Frontend Engineers");
    assertThat(entity.getDescription()).isNull();
    var firedEvent = events.stream(GroupEvent.class)
        .filter(e -> e.eventType() == GroupEventType.CREATED)
        .map(GroupEvent::source)
        .findFirst();
    assertThat(firedEvent).isPresent()
        .get().isSameAs(group);
  }

  @Test
  void shouldFindGroupById() {
    var found = groupRepository.findById(UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f"));
    assertThat(found).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("name", "Engineering")
        .hasFieldOrPropertyWithValue("description", "Engineering team members");
  }

  @Test
  void shouldFindGroupByName() {
    var found = groupRepository.findByName("Operations");
    assertThat(found).isPresent()
        .get()
        .extracting(GroupEntity::getId, GroupEntity::getName, GroupEntity::getDescription)
        .containsExactly(
            UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef"),
            "Operations",
            "Operations team");
  }

  @Test
  void shouldNotFindGroupByNonExistentName() {
    var found = groupRepository.findByName("NonExistentGroup");
    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindGroups() {
    var groups = groupRepository.findBy(Limit.of(10));
    assertThat(groups).isNotEmpty()
        .hasSize(10)
        .extractingResultOf("getName")
        .contains("Engineering", "Product Management", "Operations", "Administrators");
  }

  @Test
  void shouldFindGroupsByScrolling() {
    var window = groupRepository.findBy(Limit.of(10), Sort.by("name"), ScrollPosition.keyset());
    assertThat(window).isNotEmpty()
        .hasSize(10)
        .extractingResultOf("getName")
        .contains(
            "Administrators",
            "Business Development",
            "Compliance",
            "Content",
            "Customer Support",
            "Data Analytics",
            "Design",
            "DevOps",
            "Engineering",
            "Finance");
  }

  @Test
  void shouldFindGroupsByScrollingAfterKeys() {
    var window = groupRepository.findBy(
        Limit.of(5),
        Sort.by("name"),
        ScrollPosition.forward(Map.of(
            "name", "Finance",
            "id", UUID.fromString("d4567890-bcde-f012-3456-789abcdef012"))
        ));
    assertThat(window).isNotEmpty()
        .hasSize(5)
        .extractingResultOf("getName")
        .containsExactly(
            "Human Resources",
            "Legal",
            "Marketing",
            "Operations",
            "Product Management");
  }

  @Test
  void shouldFindGroupsWithUnlimitedScroll() {
    var window = groupRepository.findBy(Limit.unlimited(), Sort.by("name"), ScrollPosition.keyset());
    assertThat(window).isNotEmpty()
        .hasSizeGreaterThanOrEqualTo(20); // At least the test data
  }

  @Test
  void shouldDeleteGroup() {
    var group = new GroupEntity();
    group.setName("Temporary Group");
    group.setDescription("This group will be deleted");

    var entity = em.persistAndFlush(group);
    // entity was successfully saved
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNotNull();

    // delete entity (object under test)
    groupRepository.deleteById(entity.getId());
    em.flush(); // flush changes to the database

    var none = em.find(GroupEntity.class, group.getId());
    assertThat(none).isNull();
  }

  @Test
  void shouldHandleMultipleGroupsWithSimilarNames() {
    var group1 = new GroupEntity();
    group1.setName("Test Group Alpha");
    var entity1 = em.persistAndFlush(group1);

    var group2 = new GroupEntity();
    group2.setName("Test Group Beta");
    var entity2 = em.persistAndFlush(group2);

    assertThat(entity1.getId()).isNotEqualTo(entity2.getId());

    var found1 = groupRepository.findByName("Test Group Alpha");
    var found2 = groupRepository.findByName("Test Group Beta");

    assertThat(found1).isPresent();
    assertThat(found2).isPresent();
    assertThat(found1.get().getId()).isEqualTo(entity1.getId());
    assertThat(found2.get().getId()).isEqualTo(entity2.getId());
  }

  @Test
  void shouldUpdateGroupDescription() {
    var group = new GroupEntity();
    group.setName("Updatable Group");
    group.setDescription("Original description");

    var entity = groupRepository.saveAndFlush(group);
    var originalId = entity.getId();
    var originalCreatedAt = entity.getCreatedDate();

    // Update the description
    entity.setDescription("Updated description");
    var updated = groupRepository.saveAndFlush(entity);

    assertThat(updated.getId()).isEqualTo(originalId);
    assertThat(updated.getDescription()).isEqualTo("Updated description");
    assertThat(updated.getCreatedDate()).isEqualTo(originalCreatedAt);
    assertThat(updated.getLastModifiedDate()).isPresent()
        .get().isNotEqualTo(originalCreatedAt.orElse(null));
  }

  @Test
  void shouldFindGroupsUsingUnsortedScroll() {
    var window = groupRepository.findBy(Limit.of(5), Sort.unsorted(), ScrollPosition.keyset());
    assertThat(window).isNotEmpty()
        .hasSizeLessThanOrEqualTo(5);
  }
}

