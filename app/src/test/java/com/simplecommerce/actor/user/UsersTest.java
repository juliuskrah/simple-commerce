package com.simplecommerce.actor.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for the Users repository interface.
 *
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
class UsersTest {

  @Autowired
  private TestEntityManager em;

  @Autowired
  private Users userRepository;

  @Test
  void shouldFindUserById() {
    // Given
    UserEntity user = new UserEntity();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setDepartment("Engineering");
    UserEntity savedUser = em.persistFlushFind(user);
    UUID userId = savedUser.getId();
    em.detach(savedUser);

    // When
    var foundUser = userRepository.findById(userId);

    // Then
    assertThat(foundUser).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("username", "testuser")
        .hasFieldOrPropertyWithValue("email", "test@example.com")
        .hasFieldOrPropertyWithValue("department", "Engineering");
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When
    var result = userRepository.findById(nonExistentId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldFindUserByUsername() {
    // Given
    UserEntity user = new UserEntity();
    user.setUsername("johndoe");
    user.setEmail("john.doe@example.com");
    user.setDepartment("Marketing");
    em.persistAndFlush(user);
    em.detach(user);

    // When
    var foundUser = userRepository.findByUsername("johndoe");

    // Then
    assertThat(foundUser).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("username", "johndoe")
        .hasFieldOrPropertyWithValue("email", "john.doe@example.com")
        .hasFieldOrPropertyWithValue("department", "Marketing");
  }

  @Test
  void shouldReturnEmptyWhenUsernameNotFound() {
    // Given
    String nonExistentUsername = "nonexistentuser";

    // When
    var result = userRepository.findByUsername(nonExistentUsername);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldSaveAndFlushUser() {
    // Given
    UserEntity user = new UserEntity();
    user.setUsername("newuser");
    user.setEmail("new.user@example.com");
    user.setDepartment("Finance");

    // When
    UserEntity savedUser = userRepository.saveAndFlush(user);

    // Then
    assertThat(savedUser.getId()).isNotNull();

    // Verify the user was actually persisted
    em.detach(savedUser);
    var retrievedUser = em.find(UserEntity.class, savedUser.getId());
    assertThat(retrievedUser)
        .hasFieldOrPropertyWithValue("username", "newuser")
        .hasFieldOrPropertyWithValue("email", "new.user@example.com")
        .hasFieldOrPropertyWithValue("department", "Finance");
  }
}
