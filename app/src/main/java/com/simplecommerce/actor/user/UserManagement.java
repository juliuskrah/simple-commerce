package com.simplecommerce.actor.user;

import static com.simplecommerce.shared.authorization.BaseRoles.ADMINISTRATOR;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.callInScope;

import com.simplecommerce.actor.User;
import com.simplecommerce.security.aspects.Permit;
import com.simplecommerce.shared.authentication.DexIdpService;
import com.simplecommerce.shared.exceptions.NotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectFactory;
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
public class UserManagement implements UserService {

  private Users userRepository;
  private DexIdpService oidcService;

  public void setUserRepository(ObjectFactory<Users> userRepository) {
    this.userRepository = userRepository.getObject();
  }

  public void setDexIdpService(ObjectFactory<DexIdpService> dexIdpService) {
    this.oidcService = dexIdpService.getObject();
  }

  private User fromEntity(UserEntity entity) {
    return getUser(entity);
  }

  @NonNull
  public static User getUser(UserEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new User(
        entity.getId().toString(),
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

  @Permit(namespace = "Role", object = "'Administrator'", relation = "assignees")
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
}
