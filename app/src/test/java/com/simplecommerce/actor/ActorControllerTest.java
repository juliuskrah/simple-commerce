package com.simplecommerce.actor;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.Role;
import com.simplecommerce.shared.types.Types;
import com.simplecommerce.shared.types.UserType;
import graphql.ErrorType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.GraphQlTester;
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
    User user = new User(USER_ID, "testuser", UserType.COLLABORATOR, now, now, now, "test@example.com");
    when(actorService.findActor("testuser")).thenReturn(Optional.of(user));

    graphQlTester.documentName("actorDetails")
        .operationName("actor")
        .variable("username", "testuser")
        .execute()
        .path("actor").entity(User.class)
        .satisfies(actor -> {
          assertThat(actor).isNotNull()
              .extracting(Actor::username, as(InstanceOfAssertFactories.STRING))
              .isEqualTo("testuser");
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
    User user = new User(USER_ID, "testuser", UserType.STAFF, now, now, now, "test@example.com");
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

  @Test
  @DisplayName("Should show all permissions in the system")
  void shouldShowAllPermissions() {
    when(actorService.findPermissions()).thenReturn(Arrays.asList(BasePermissions.values()));

    graphQlTester.documentName("actorDetails")
        .operationName("permissions")
        .execute()
        .path("permissions")
        .hasValue().entity(BasePermissions[].class).satisfies(permissions -> assertThat(permissions).isNotNull().hasSize(8)
            .containsExactly(BasePermissions.values()));
  }

  @Test
  @DisplayName("Should show custom and built-in roles in the system")
  void shouldShowAllRoles() {
    var roles = List.of(new Role("Administrator", List.of(BasePermissions.DELETE_PRODUCTS)));
    when(actorService.findRoles()).thenReturn(roles);

    graphQlTester.documentName("actorDetails")
        .operationName("roles")
        .execute()
        .path("roles")
        .hasValue().entity(new ParameterizedTypeReference<List<Role>>() {
        }).satisfies(baseRoles -> {
          assertThat(baseRoles).isNotNull().hasSize(1)
              .containsExactly(roles.toArray(new Role[0]));
        });
  }
}
