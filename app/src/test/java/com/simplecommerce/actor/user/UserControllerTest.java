package com.simplecommerce.actor.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.actor.User;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.types.Types;
import com.simplecommerce.shared.types.UserType;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test for {@link UserController}.
 *
 * @author julius.krah
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@GraphQlTest(UserController.class)
class UserControllerTest {

  private static final String USER_ID = "18d25652-5870-4555-8146-5166fec97c3f";
  private final OffsetDateTime now = OffsetDateTime.now();
  @Autowired
  private GraphQlTester graphQlTester;

  @MockitoBean
  private UserService userService;

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Should fetch the current user")
  void shouldFetchCurrentUser() {
    User user = new User(USER_ID, "testuser", UserType.STAFF, now, now, now, "test@example.com");
    when(userService.findUser(anyString())).thenReturn(user);

    graphQlTester.documentName("userDetails")
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
    User user = new User(USER_ID, "testuser", UserType.CUSTOMER, now, now, now, "test@example.com");
    when(userService.findUser(anyString())).thenReturn(user);

    String expectedGlobalId = new GlobalId(Types.NODE_USER, USER_ID).encode();

    graphQlTester.documentName("userDetails")
        .operationName("me")
        .execute()
        .path("me.id").entity(String.class)
        .isEqualTo(expectedGlobalId);
  }

  @Test
  @DisplayName("Should create user")
  void shouldCreateNewUser(@Captor ArgumentCaptor<CreateUserInput> captor) {
    User user = new User(USER_ID, "testuser", UserType.CUSTOMER, now, now, now, "test@example.com");
    when(userService.createUser(captor.capture())).thenReturn(Optional.of(user));

    graphQlTester.documentName("userDetails")
        .operationName("createUser")
        .variables(Map.of("input", Map.of("username", "testuser", "email", "testuser@example.com")))
        .execute()
        .path("addUser").entity(User.class)
        .satisfies(fetchedUser -> assertThat(fetchedUser).isNotNull()
            .extracting(User::id).asString().isBase64());

    assertThat(captor.getValue()).isNotNull()
        .extracting(CreateUserInput::userType).isEqualTo(UserType.STAFF); // Default is STAFF
  }
}
