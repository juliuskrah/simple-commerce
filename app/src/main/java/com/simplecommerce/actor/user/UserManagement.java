package com.simplecommerce.actor.user;

import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.ROLE_NAMESPACE;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.callInScope;
import static java.util.Objects.requireNonNull;

import com.simplecommerce.actor.User;
import com.simplecommerce.node.Node;
import com.simplecommerce.node.NodeService;
import com.simplecommerce.security.aspects.Permit;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authentication.DexIdpService;
import com.simplecommerce.shared.exceptions.NotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management services.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class UserManagement implements UserService, NodeService {

  private Users userRepository;
  @Nullable
  private DexIdpService oidcService;

  public void setUserRepository(ObjectFactory<Users> userRepository) {
    this.userRepository = userRepository.getObject();
  }

  public void setDexIdpService(ObjectProvider<DexIdpService> dexIdpService) {
    this.oidcService = dexIdpService.getIfAvailable();
  }

  private User fromEntity(UserEntity entity) {
    return getUser(entity);
  }

  @NonNull
  public static User getUser(UserEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new User(
        requireNonNull(entity.getId()).toString(),
        entity.getUsername(),
        entity.getUserType(),
        entity.getLastModifiedDate().orElseGet(epoch),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getLastLogin(),
        entity.getEmail()
    );
  }

  @Override
  public User findUser(String username) {
    return callInScope(() -> userRepository.findByUsername(username).map(this::fromEntity).orElseThrow(NotFoundException::new));
  }

  @Override
  public List<User> findUsers(List<String> usernames) {
    return userRepository.findByUsernameIn(usernames).stream().map(this::fromEntity).toList();
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public Optional<User> createUser(CreateUserInput user) {
    var userPassword = user.username().toLowerCase() + "123";
    var alreadyExist = oidcService.addUser(user.email(), user.username(), userPassword);
    if (alreadyExist) {
      return Optional.empty();
    }
    var entity = new UserEntity();
    entity.setUsername(user.username());
    entity.setEmail(user.email());
    entity.setUserType(user.userType());
    userRepository.saveAndFlush(entity);
    return Optional.of(fromEntity(entity));
  }

  @Override
  public User node(String id) {
    var gid = GlobalId.decode(id);
    return callInScope(() -> userRepository.findById(UUID.fromString(gid.id())).map(this::fromEntity).orElseThrow(NotFoundException::new));
  }
}
