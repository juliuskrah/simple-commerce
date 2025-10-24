package com.simplecommerce.actor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import com.simplecommerce.shared.types.PermissionTupleInput;
import com.simplecommerce.shared.types.PermissionTupleInput.SubjectInput;
import com.simplecommerce.shared.types.PermissionTupleInput.SubjectSetInput;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sh.ory.keto.relation_tuples.v1alpha2.Subject;
import sh.ory.keto.relation_tuples.v1alpha2.SubjectSet;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta.Action;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;

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
  void shouldAddPermissionsToActor(@Captor ArgumentCaptor<TransactRelationTuplesRequest> relationCaptor) {
    // Given
    UserEntity userEntity = new UserEntity();
    userEntity.setId(USER_ID);
    userEntity.setUsername("neo");
    userEntity.setEmail("neo@example.com");
    when(actorRepository.findByUsername(anyString())).thenReturn(Optional.of(userEntity));
    doNothing().when(ketoAuthorizationService).transactRelationship(relationCaptor.capture());

    // When
    List<PermissionTupleInput> permissions = List.of(
        new PermissionTupleInput(
            "Category", "Media > Videos > Digital Download", "viewers",
            new SubjectInput(null, new SubjectSetInput("Group", "staff", "members"))
        )
    );
    var result = actorManagement.addPermissionsToActor("neo", permissions);

    // Then
    assertThat(result).isPresent();
    // Verify the captured relation tuples request
    var capturedRequest = relationCaptor.getValue();
    assertThat(capturedRequest.getRelationTupleDeltasList()).hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue("action", Action.ACTION_INSERT)
        .extracting(RelationTupleDelta::getRelationTupleOrBuilder)
        .extracting("subject")
        .hasFieldOrPropertyWithValue("id", "")
        .usingRecursiveComparison()
        .isEqualTo(Subject.newBuilder().setSet(
            SubjectSet.newBuilder()
                .setNamespace("Group")
                .setObject("staff")
                .setRelation("members")).build());
  }

  @Test
  void shouldRemovePermissionsFromActor(@Captor ArgumentCaptor<TransactRelationTuplesRequest> relationCaptor) {
    // Given
    UserEntity userEntity = new UserEntity();
    userEntity.setId(USER_ID);
    userEntity.setUsername("trinity");
    userEntity.setEmail("neo@example.com");
    when(actorRepository.findByUsername(anyString())).thenReturn(Optional.of(userEntity));
    doNothing().when(ketoAuthorizationService).transactRelationship(relationCaptor.capture());

    // When
    List<PermissionTupleInput> permissions = List.of(
        new PermissionTupleInput(
            "Category", "Media > Videos > Digital Download", "viewers",
            new SubjectInput("trinity", null)
        )
    );
    var result = actorManagement.removePermissionsFromActor("trinity", permissions);

    // Then
    assertThat(result).isPresent();
    // Verify the captured relation tuples request
    var capturedRequest = relationCaptor.getValue();
    assertThat(capturedRequest.getRelationTupleDeltasList()).hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue("action", Action.ACTION_DELETE)
        .extracting(RelationTupleDelta::getRelationTupleOrBuilder)
        .extracting("subject")
        .isEqualTo(Subject.newBuilder().setId("trinity").build());
  }
}
