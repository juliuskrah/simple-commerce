package com.simplecommerce.actor;

import static com.simplecommerce.actor.user.UserManagement.getUser;
import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.ROLE_NAMESPACE;
import static java.util.Objects.requireNonNull;

import com.simplecommerce.actor.ActorEvent.ActorEventType;
import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.group.GroupManagement;
import com.simplecommerce.actor.group.GroupService;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.actor.user.UserManagement;
import com.simplecommerce.actor.user.UserService;
import com.simplecommerce.security.aspects.Permit;
import com.simplecommerce.shared.Event;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.authorization.BuiltIns;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.types.ResourcePermittedInput;
import com.simplecommerce.shared.types.RoleAssigneeInput;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
  private static final String EDITORS_RELATION = "editors";
  private static final String VIEWERS_RELATION = "viewers";
  private static final String OWNERS_RELATION = "owners";

  private Actor fromEntity(ActorEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return switch (entity) {
      case UserEntity user -> fromEntity(user);
      case BotEntity bot -> new Bot(
          requireNonNull(bot.getId(), "Bot ID not generated").toString(),
          bot.getUsername(),
          bot.getLastModifiedDate().orElseGet(epoch),
          bot.getCreatedDate().orElseGet(epoch),
          "",
          List.of()
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
  public ResourcePermissible assignProductPermission(ResourcePermittedInput subject, List<String> productIds, BasePermissions permission) {
    var decodedProductIds = productIds.stream()
        .map(GlobalId::decode)
        .map(GlobalId::id)
        .toList();
    var relation = switch (permission) {
      case CREATE_AND_EDIT_PRODUCTS, EDIT_PRODUCT_COSTS, EDIT_PRODUCT_PRICES -> EDITORS_RELATION;
      case VIEW_PRODUCTS, VIEW_PRODUCT_COSTS, EXPORT_PRODUCTS -> VIEWERS_RELATION;
      case DELETE_PRODUCTS -> OWNERS_RELATION;
      default -> throw new IllegalStateException("Unknown product permission: " + permission);
    };
    if (Objects.nonNull(subject.actor())) {
      User user = userService.findUser(subject.actor());
      event.fire(new ActorEvent<>(user, Map.of("relation", relation, "productIds", decodedProductIds), ActorEventType.ACTOR_PRODUCT_PERMISSION_ASSIGNED));
      return user;
    } else if (Objects.nonNull(subject.group())) {
      return groupService.findGroup(subject.group()).map(group -> {
        event.fire(new ActorEvent<>(group, Map.of("relation", relation, "productIds", decodedProductIds), ActorEventType.GROUP_PRODUCT_PERMISSION_ASSIGNED));
        return group;
      }).orElseThrow(NotFoundException::new);
    } else if (Objects.nonNull(subject.role())) {
      return findRoleByName(requireNonNull(subject.role())).map(role -> {
        event.fire(new ActorEvent<>(role, Map.of("relation", relation, "productIds", decodedProductIds), ActorEventType.ROLE_PRODUCT_PERMISSION_ASSIGNED));
        return role;
      }).orElseThrow(NotFoundException::new);
    }
    throw new IllegalArgumentException("Either username, groupId or role must be provided in subject");
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public ResourcePermissible revokeProductPermission(ResourcePermittedInput subject, List<String> productIds, BasePermissions permission) {
    throw new UnsupportedOperationException("revokeProductPermission not yet implemented");
  }

  @Override
  public Optional<Role> findRoleByName(String roleName) {
    return Arrays.stream(BuiltIns.DEFAULT_ROLES)
        .filter(role -> role.getName().equals(roleName))
        .findFirst()
        .map(role -> new Role(role.getName(), null, Arrays.asList(role.getPermissions())));
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public RoleAssignable addRolesToSubject(List<String> roles, RoleAssigneeInput subject) {
    if (Objects.nonNull(subject.actor())) {
      User user = userService.findUser(subject.actor());
      event.fire(new ActorEvent<>(user, Map.of("roles", roles), ActorEventType.USER_ROLE_ASSIGNED));
      return user;
    } else if (Objects.nonNull(subject.group())) {
      return groupService.findGroup(subject.group()).map(group -> {
        event.fire(new ActorEvent<>(group, Map.of("roles", roles), ActorEventType.GROUP_ROLE_ASSIGNED));
        return group;
      }).orElseThrow(NotFoundException::new);
    }
    throw new IllegalArgumentException("Either username or groupId must be provided in subject");
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public RoleAssignable removeRolesFromSubject(List<String> roles, RoleAssigneeInput subject) {
    throw new UnsupportedOperationException("removeRolesFromSubject not yet implemented");
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public List<Role> findRoles() {
    var roles = Arrays.stream(BuiltIns.DEFAULT_ROLES);
    return roles.map(role -> new Role(
        role.getName(), null, Arrays.asList(role.getPermissions())
    )).toList();
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public List<BasePermissions> findPermissions() {
    return Arrays.asList(BuiltIns.DEFAULT_PERMISSIONS);
  }
}
