package com.simplecommerce.actor;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.shared.types.UserType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for the Actors repository interface.
 *
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
class ActorsTest {

  @Autowired
  private TestEntityManager em;

  @Autowired
  private Actors actorRepository;

  @Test
  void shouldFindActorById() {
    // Given
    UserEntity user = new UserEntity();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setUserType(UserType.STAFF);
    UserEntity savedUser = em.persistFlushFind(user);
    UUID userId = savedUser.getId();
    em.detach(savedUser);

    // When
    var foundActor = actorRepository.findById(userId);

    // Then
    assertThat(foundActor).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("username", "testuser")
        .hasFieldOrPropertyWithValue("email", "test@example.com");
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When
    var result = actorRepository.findById(nonExistentId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldFindBotById() {
    // Given
    BotEntity bot = new BotEntity();
    bot.setUsername("testbot");
    bot.setEmail("bot@example.com");
    bot.setApiKey("api-key-123456");
    bot.setAppId("app-123");
    bot.setPermissions("[\"read\", \"write\"]");
    BotEntity savedBot = em.persistFlushFind(bot);
    UUID botId = savedBot.getId();
    em.detach(savedBot);

    // When
    var foundActor = actorRepository.findById(botId);

    // Then
    assertThat(foundActor).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("username", "testbot")
        .hasFieldOrPropertyWithValue("email", "bot@example.com")
        .hasFieldOrPropertyWithValue("apiKey", "api-key-123456")
        .hasFieldOrPropertyWithValue("appId", "app-123")
        .hasFieldOrPropertyWithValue("permissions", "[\"read\", \"write\"]");
  }

  @Test
  void shouldSaveBot() {
    // Given
    BotEntity bot = new BotEntity();
    bot.setUsername("newbot");
    bot.setEmail("newbot@example.com");
    bot.setApiKey("new-api-key-789");
    bot.setAppId("app-789");
    bot.setPermissions("[\"admin\"]");

    // When
    BotEntity savedBot = actorRepository.saveAndFlush(bot);

    // Then
    assertThat(savedBot).isNotNull()
        .hasFieldOrPropertyWithValue("username", "newbot")
        .hasFieldOrPropertyWithValue("email", "newbot@example.com")
        .hasFieldOrPropertyWithValue("apiKey", "new-api-key-789")
        .hasFieldOrPropertyWithValue("appId", "app-789")
        .hasFieldOrPropertyWithValue("permissions", "[\"admin\"]")
        .hasNoNullFieldsOrPropertiesExcept("lastLogin", "createdBy", "updatedBy", "externalId");

    // Verify it can be retrieved
    UUID botId = savedBot.getId();
    em.detach(savedBot);
    var retrievedBot = actorRepository.findById(botId);
    assertThat(retrievedBot).isPresent();
  }

  @Test
  void shouldFindUserByUsername() {
    // Given
    UserEntity user = new UserEntity();
    user.setUsername("uniqueuser");
    user.setEmail("unique@example.com");
    user.setUserType(UserType.COLLABORATOR);
    em.persistAndFlush(user);
    em.detach(user);

    // When
    var foundActor = actorRepository.findByUsername("uniqueuser");

    // Then
    assertThat(foundActor).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("username", "uniqueuser")
        .hasFieldOrPropertyWithValue("email", "unique@example.com")
        .hasFieldOrPropertyWithValue("userType", UserType.COLLABORATOR);
  }

  @Test
  void shouldFindBotByUsername() {
    // Given
    BotEntity bot = new BotEntity();
    bot.setUsername("uniquebot");
    bot.setEmail("uniquebot@example.com");
    bot.setApiKey("unique-api-key");
    bot.setAppId("unique-app");
    em.persistAndFlush(bot);
    em.detach(bot);

    // When
    var foundActor = actorRepository.findByUsername("uniquebot");

    // Then
    assertThat(foundActor).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("username", "uniquebot")
        .hasFieldOrPropertyWithValue("email", "uniquebot@example.com")
        .hasFieldOrPropertyWithValue("apiKey", "unique-api-key")
        .hasFieldOrPropertyWithValue("appId", "unique-app");
  }

  @Test
  void shouldReturnEmptyWhenUsernameNotFound() {
    // Given
    String nonExistentUsername = "nonexistentuser";

    // When
    var result = actorRepository.findByUsername(nonExistentUsername);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnCorrectActorTypeWhenFindingByUsername() {
    // Given
    UserEntity user = new UserEntity();
    user.setUsername("usertype");
    user.setEmail("usertype@example.com");
    user.setUserType(UserType.CUSTOMER);
    em.persist(user);

    BotEntity bot = new BotEntity();
    bot.setUsername("bottype");
    bot.setEmail("bottype@example.com");
    bot.setApiKey("type-api-key");
    em.persistAndFlush(bot);
    em.detach(user);
    em.detach(bot);

    // When
    var foundUser = actorRepository.findByUsername("usertype");
    var foundBot = actorRepository.findByUsername("bottype");

    // Then
    assertThat(foundUser).isPresent()
        .get()
        .isInstanceOf(UserEntity.class);

    assertThat(foundBot).isPresent()
        .get()
        .isInstanceOf(BotEntity.class);
  }
}
