package com.simplecommerce.actor;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.Types;
import com.simplecommerce.shared.types.UserType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    Bot bot = new Bot(botId, "testbot", now, now, "", List.of());
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
    var roles = List.of(new Role("Administrator", null, List.of(BasePermissions.DELETE_PRODUCTS)));
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
