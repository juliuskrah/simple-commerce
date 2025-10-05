package com.simplecommerce.actor;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.types.Types;
import graphql.ErrorType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test for {@link ActorController}.
 *
 * @author julius.krah
 * @since 1.0
 */
@GraphQlTest(ActorController.class)
class ActorControllerTest {

  @Autowired
  private GraphQlTester graphQlTester;

  @MockitoBean
  private ActorService actorService;

  private final OffsetDateTime now = OffsetDateTime.now();
  private static final String USER_ID = "18d25652-5870-4555-8146-5166fec97c3f";

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Should fetch the current user")
  void shouldFetchCurrentUser() {
    User user = new User(USER_ID, "testuser", now, now, now, "test@example.com");
    when(actorService.findUser(anyString())).thenReturn(user);

    graphQlTester.documentName("actorDetails")
        .operationName("me")
        .execute()
        .path("me").entity(User.class)
        .satisfies(fetchedUser -> {
          assertThat(fetchedUser).isNotNull();
          assertThat(fetchedUser.id()).isEqualTo(new GlobalId(Types.NODE_USER, USER_ID).encode());
          assertThat(fetchedUser.username()).isEqualTo("testuser");
          assertThat(fetchedUser.email()).isEqualTo("test@example.com");
        });
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Should return correct global ID for User")
  void shouldReturnCorrectGlobalIdForUser() {
    User user = new User(USER_ID, "testuser", now, now, now, "test@example.com");
    when(actorService.findUser(anyString())).thenReturn(user);

    String expectedGlobalId = new GlobalId(Types.NODE_USER, USER_ID).encode();

    graphQlTester.documentName("actorDetails")
        .operationName("me")
        .execute()
        .path("me.id").entity(String.class)
        .isEqualTo(expectedGlobalId);
  }

  @Test
  @DisplayName("Should return correct global ID for Bot")
  void shouldReturnCorrectGlobalIdForBot() {
    String botId = "28e35762-6980-5666-9257-6277gfd08d4g";
    Bot bot = new Bot(botId, "testbot", now, now);
    when(actorService.findActor("testbot")).thenReturn(Optional.of(bot));

    String expectedGlobalId = new GlobalId(Types.NODE_BOT, botId).encode();

    graphQlTester.documentName("actorDetails")
        .operationName("actor")
        .variable("username", "testbot")
        .execute()
        .path("actor.id").entity(String.class)
        .isEqualTo(expectedGlobalId);
  }

  @Test
  @DisplayName("Should fetch actor by username")
  void shouldFetchActorByUsername() {
    User user = new User(USER_ID, "testuser", now, now, now, "test@example.com");
    when(actorService.findActor("testuser")).thenReturn(Optional.of(user));

    graphQlTester.documentName("actorDetails")
        .operationName("actor")
        .variable("username", "testuser")
        .execute()
        .path("actor").entity(User.class)
        .satisfies(actor -> {
          assertThat(actor).isNotNull()
              .extracting(Actor::id, as(InstanceOfAssertFactories.STRING))
              .isEqualTo(new GlobalId(Types.NODE_USER, USER_ID).encode());
          assertThat(actor).extracting(Actor::username).isEqualTo("testuser");
        });
  }

  @Test
  @DisplayName("Should return null when actor is not found")
  void shouldReturnNullWhenActorNotFound() {
    when(actorService.findActor("unknownuser")).thenReturn(Optional.empty());

    graphQlTester.documentName("actorDetails")
        .operationName("actor")
        .variable("username", "unknownuser")
        .execute()
        .path("actor").valueIsNull();
  }

  @Test
  @DisplayName("Should assign permissions to actor")
  void shouldAssignPermissionsToActor() {
    Bot bot = new Bot(UUID.randomUUID().toString(), "testbot", now, now);
    when(actorService.addPermissionsToActor(anyString(), anyList())).thenReturn(Optional.of(bot));
    List<Map<String, Object>> permissions = List.of(Map.of(
            "namespace", "Product",
            "object", " The Matrix",
            "relation", "viewer",
            "subject", Map.of("subjectId", "neo")
        )
    );
    graphQlTester.documentName("actorPermissions")
        .operationName("assignPermissions")
        .variable("username", "testuser")
        .variable("permissions", permissions)
        .execute()
        .path("assignPermissionsToActor")
        .entity(Bot.class)
        .satisfies(actor -> assertThat(actor).isNotNull().hasFieldOrPropertyWithValue("username", "testbot"));
  }

  @Test
  @DisplayName("Should revoke permissions from actor")
  void shouldRevokePermissionsFromActor() {
    User user = new User(USER_ID, "testuser", now, now, now, "test@example.com");
    when(actorService.removePermissionsFromActor(anyString(), anyList())).thenReturn(Optional.of(user));
    List<Map<String, Object>> permissions = List.of(Map.of(
            "namespace", "Product",
            "object", "The Matrix",
            "relation", "viewer",
            "subject", Map.of("subjectId", "neo")
        )
    );
    graphQlTester.documentName("actorPermissions")
        .operationName("revokePermissions")
        .variable("username", "testuser")
        .variable("permissions", permissions)
        .execute()
        .path("revokePermissionsFromActor")
        .entity(User.class)
        .satisfies(actor -> assertThat(actor).isNotNull().hasFieldOrPropertyWithValue("username", "testuser"));
  }

  @Test
  @DisplayName("Should validate permission tuple with subjectId")
  void shouldValidatePermissionTupleWithSubjectId() {
    List<Map<String, Object>> permissions = List.of(Map.of(
        "namespace", "Product",
        "object", "The Matrix",
        "relation", "viewer",
        "subject", Map.of("subjectId", "neo")
    ));

    graphQlTester.documentName("actorPermissions")
        .operationName("assignPermissions")
        .variable("username", "testuser")
        .variable("permissions", permissions)
        .execute().path("assignPermissionsToActor").valueIsNull();
  }

  @Test
  @DisplayName("Should validate permission tuple with subjectSet")
  void shouldValidatePermissionTupleWithSubjectSet() {
    List<Map<String, Object>> permissions = List.of(Map.of(
        "namespace", "Product",
        "object", "The Matrix",
        "relation", "viewer",
        "subject", Map.of("subjectSet", Map.of(
            "namespace", "Group",
            "object", "admin",
            "relation", "member"
        ))
    ));

    graphQlTester.documentName("actorPermissions")
        .operationName("assignPermissions")
        .variable("username", "testuser")
        .variable("permissions", permissions)
        .execute()
        .errors().verify();
  }

  @Test
  @DisplayName("Should reject permission tuple with both subjectId and subjectSet")
  void shouldRejectPermissionTupleWithBothSubjectIdAndSubjectSet() {
    List<Map<String, Object>> permissions = List.of(Map.of(
        "namespace", "Product",
        "object", "The Matrix",
        "relation", "viewer",
        "subject", Map.of(
            "subjectId", "neo",
            "subjectSet", Map.of(
                "namespace", "Group",
                "object", "admin",
                "relation", "member"
            )
        )
    ));

    graphQlTester.documentName("actorPermissions")
        .operationName("assignPermissions")
        .variable("username", "testuser")
        .variable("permissions", permissions)
        .execute()
        .errors()
        .satisfy(errors -> assertThat(errors).isNotEmpty().hasSize(1)
            .element(0)
            .hasFieldOrPropertyWithValue("message", "Exactly one key must be specified for OneOf type 'SubjectInput'.")
            .hasFieldOrPropertyWithValue("errorType", ErrorType.ValidationError));
  }

  @Test
  @DisplayName("Should reject permission tuple with neither subjectId nor subjectSet")
  void shouldRejectPermissionTupleWithNeitherSubjectIdNorSubjectSet() {
    List<Map<String, Object>> permissions = List.of(Map.of(
        "namespace", "Product",
        "object", "The Matrix",
        "relation", "viewer",
        "subject", Map.of()
    ));

    graphQlTester.documentName("actorPermissions")
        .operationName("assignPermissions")
        .variable("username", "testuser")
        .variable("permissions", permissions)
        .execute()
        .errors()
        .satisfy(errors -> assertThat(errors).isNotEmpty().hasSize(1)
            .element(0)
            .hasFieldOrPropertyWithValue("message", "Exactly one key must be specified for OneOf type 'SubjectInput'.")
            .hasFieldOrPropertyWithValue("errorType", ErrorType.ValidationError));
  }
}
