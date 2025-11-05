package com.simplecommerce.actor;

import static com.simplecommerce.actor.user.UserManagement.getUser;
import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.ROLE_NAMESPACE;

import com.simplecommerce.actor.ActorEvent.ActorEventType;
import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.group.GroupManagement;
import com.simplecommerce.actor.group.GroupService;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.actor.user.UserManagement;
import com.simplecommerce.actor.user.UserService;
import com.simplecommerce.security.aspects.Permit;
import com.simplecommerce.shared.Event;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.authorization.BuiltIns;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.types.Role;
import com.simplecommerce.shared.types.SubjectInput;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Actor management related classes and services.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class ActorManagement implements ActorService {

  public void setActorRepository(ObjectFactory<Actors> actorRepository) {
    this.actorRepository = actorRepository.getObject();
  }

  public void setEvent(ObjectFactory<Event<ActorEvent<?>>> event) {
    this.event = event.getObject();
  }

  private Actors actorRepository;
  private Event<ActorEvent<?>> event;
  private final UserService userService = new UserManagement();
  private final GroupService groupService = new GroupManagement();

  private Actor fromEntity(ActorEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return switch (entity) {
      case UserEntity user -> fromEntity(user);
      case BotEntity bot -> new Bot(
          Objects.requireNonNull(bot.getId(), "Bot ID not generated").toString(),
          bot.getUsername(),
          bot.getLastModifiedDate().orElseGet(epoch),
          bot.getCreatedDate().orElseGet(epoch)
      );
      default -> throw new IllegalStateException("Unknown actor entity type: " + entity.getClass());
    };
  }

  private User fromEntity(UserEntity entity) {
    return getUser(entity);
  }

  @Override
  public Optional<Actor> findActor(@NonNull String username) {
    return actorRepository.findByUsername(username).map(this::fromEntity);
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public PermissionAssignmentPayload removeRolesFromSubject(List<String> roles, SubjectInput subject) {
    throw new UnsupportedOperationException("removeRolesFromSubject not yet implemented");
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public RoleAssignable addRolesToSubject(List<String> roles, SubjectInput subject) {
    if (Objects.nonNull(subject.actor())) {
      User user = userService.findUser(subject.actor());
      event.fire(new ActorEvent<>(user, roles, ActorEventType.USER_ROLE_ASSIGNED));
      return user;
    } else if (Objects.nonNull(subject.group())) {
      return groupService.findGroup(subject.group()).map(group -> {
        event.fire(new ActorEvent<>(group, roles, ActorEventType.GROUP_ROLE_ASSIGNED));
        return group;
      }).orElseThrow(NotFoundException::new);
    }
    throw new IllegalArgumentException("Either username or groupId must be provided in subject");
  }

  @Override
  public RoleAssignable revokeRolesFromSubject(List<String> roles, SubjectInput subject) {
    throw new UnsupportedOperationException("revokeRolesFromSubject not yet implemented");
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public List<Role> findRoles() {
    var roles = Arrays.stream(BuiltIns.DEFAULT_ROLES);
    return roles.map(role -> new Role(
        role.getName(), Arrays.asList(role.getPermissions())
    )).toList();
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public List<BasePermissions> findPermissions() {
    return Arrays.asList(BuiltIns.DEFAULT_PERMISSIONS);
  }
}
