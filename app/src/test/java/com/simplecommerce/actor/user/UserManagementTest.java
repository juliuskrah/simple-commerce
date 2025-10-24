package com.simplecommerce.actor.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.actor.User;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import com.simplecommerce.shared.exceptions.NotFoundException;
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
class UserManagementTest {

  @Mock
  private Users userRepository;

  @Mock
  private KetoAuthorizationService ketoAuthorizationService;

  @InjectMocks
  private UserManagement actorManagement;

  private static final UUID USER_ID = UUID.fromString("18d25652-5870-4555-8146-5166fec97c3f");

  @Test
  void shouldFindUserByUsername() {
    // Given
    UserEntity userEntity = new UserEntity();
    userEntity.setId(USER_ID);
    userEntity.setUsername("testuser");
    userEntity.setEmail("test@example.com");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

    // When
    User user = actorManagement.findUser("testuser");

    // Then
    assertThat(user).isNotNull()
        .hasFieldOrPropertyWithValue("id", USER_ID.toString())
        .hasFieldOrPropertyWithValue("username", "testuser")
        .hasFieldOrPropertyWithValue("email", "test@example.com");
  }

  @Test
  void shouldThrowNotFoundExceptionWhenUserNotFound() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    // When
    var throwable = catchThrowable(() -> actorManagement.findUser("nonexistent"));

    // Then
    assertThat(throwable).isInstanceOf(NotFoundException.class);
  }
}
