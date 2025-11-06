package com.simplecommerce.actor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for the ActorManagement service implementation.
 *
 * @author julius.krah
 */
@ExtendWith(MockitoExtension.class)
class ActorManagementTest {

  @Mock
  private Actors actorRepository;

  @Mock
  private KetoAuthorizationService ketoAuthorizationService;

  @InjectMocks
  private ActorManagement actorManagement;

  private static final UUID USER_ID = UUID.fromString("18d25652-5870-4555-8146-5166fec97c3f");
  private static final UUID BOT_ID = UUID.fromString("28e35762-6980-5666-9257-6277fed08d4f");

  @Test
  void shouldFindUserActorByUsername() {
    // Given
    UserEntity userEntity = new UserEntity();
    userEntity.setId(USER_ID);
    userEntity.setUsername("testuser");
    userEntity.setEmail("test@example.com");
    when(actorRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

    // When
    var actor = actorManagement.findActor("testuser");

    // Then
    assertThat(actor).isPresent()
        .get()
        .isInstanceOf(User.class)
        .hasFieldOrPropertyWithValue("id", USER_ID.toString())
        .hasFieldOrPropertyWithValue("username", "testuser")
        .hasFieldOrPropertyWithValue("email", "test@example.com");
  }

  @Test
  void shouldFindBotActorByUsername() {
    // Given
    BotEntity botEntity = new BotEntity();
    botEntity.setId(BOT_ID);
    botEntity.setUsername("testbot");
    when(actorRepository.findByUsername("testbot")).thenReturn(Optional.of(botEntity));

    // When
    var actor = actorManagement.findActor("testbot");

    // Then
    assertThat(actor).isPresent()
        .get()
        .isInstanceOf(Bot.class)
        .hasFieldOrPropertyWithValue("id", BOT_ID.toString())
        .hasFieldOrPropertyWithValue("username", "testbot");
  }

  @Test
  void shouldReturnEmptyOptionalWhenActorNotFound() {
    // Given
    when(actorRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    // When
    var actor = actorManagement.findActor("nonexistent");

    // Then
    assertThat(actor).isEmpty();
  }

  @Test
  void shouldFindRoleByName() {
    // When
    var role = actorManagement.findRoleByName("Administrator");

    // Then
    assertThat(role).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("name", "Administrator")
        .extracting(Role::permissions)
        .satisfies(permissions -> assertThat((List<?>) permissions).isEmpty());
  }

  @Test
  void shouldFindRoleWithPermissions() {
    // When
    var role = actorManagement.findRoleByName("Merchandiser");

    // Then
    assertThat(role).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("name", "Merchandiser")
        .satisfies(r -> {
          assertThat(r.permissions()).hasSize(2);
          assertThat(r.permissions()).contains(
              BasePermissions.DELETE_PRODUCTS,
              BasePermissions.VIEW_DASHBOARD
          );
        });
  }

  @Test
  void shouldReturnEmptyOptionalWhenRoleNotFound() {
    // When
    var role = actorManagement.findRoleByName("NonExistentRole");

    // Then
    assertThat(role).isEmpty();
  }

  @Test
  void shouldFindAllBuiltInRoles() {
    // When
    var role1 = actorManagement.findRoleByName("Administrator");
    var role2 = actorManagement.findRoleByName("Online store editor");
    var role3 = actorManagement.findRoleByName("Customer support");
    var role4 = actorManagement.findRoleByName("Merchandiser");
    var role5 = actorManagement.findRoleByName("Marketer");

    // Then
    assertThat(role1).isPresent();
    assertThat(role2).isPresent();
    assertThat(role3).isPresent();
    assertThat(role4).isPresent();
    assertThat(role5).isPresent();
  }
}
