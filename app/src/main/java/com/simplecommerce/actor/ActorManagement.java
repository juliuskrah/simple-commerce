package com.simplecommerce.actor;

import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.actor.user.Users;
import com.simplecommerce.shared.authorization.BuiltIns;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.types.PermissionTupleInput;
import com.simplecommerce.shared.types.Role;
import com.simplecommerce.shared.types.Role.Permission;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import sh.ory.keto.relation_tuples.v1alpha2.RelationTuple;
import sh.ory.keto.relation_tuples.v1alpha2.Subject;
import sh.ory.keto.relation_tuples.v1alpha2.SubjectSet;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta.Action;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;

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

  public void setUserRepository(ObjectFactory<Users> userRepository) {
    this.userRepository = userRepository.getObject();
  }

  public void setKetoAuthorizationService(ObjectFactory<KetoAuthorizationService> ketoAuthorizationService) {
    this.ketoAuthorizationService = ketoAuthorizationService.getObject();
  }

  private Actors actorRepository;
  private Users userRepository;
  private KetoAuthorizationService ketoAuthorizationService;

  private Actor fromEntity(ActorEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return switch (entity) {
      case UserEntity user -> fromEntity(user);
      case BotEntity bot -> new Bot(
          bot.getId().toString(),
          bot.getUsername(),
          bot.getLastModifiedDate().orElseGet(epoch),
          bot.getCreatedDate().orElseGet(epoch)
      );
      default -> throw new IllegalStateException("Unknown actor entity type: " + entity.getClass());
    };
  }

  private User fromEntity(UserEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new User(
        entity.getId().toString(),
        entity.getUsername(),
        entity.getLastModifiedDate().orElseGet(epoch),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getLastLogin(),
        entity.getEmail()
    );
  }

  private TransactRelationTuplesRequest toRelationTuplesRequest(Action action, List<PermissionTupleInput> permissions) {
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var permission : permissions) {
      var subjectBuilder = Subject.newBuilder();
      if (Objects.nonNull(permission.subject().subjectId())) {
        subjectBuilder.setId(permission.subject().subjectId());
      } else if (Objects.nonNull(permission.subject().subjectSet())) {
        subjectBuilder.setSet(SubjectSet.newBuilder()
            .setNamespace(permission.subject().subjectSet().namespace())
            .setObject(permission.subject().subjectSet().object())
            .setRelation(permission.subject().subjectSet().relation()));
      }

      var deltaBuilder = RelationTupleDelta.newBuilder()
          .setAction(action);
      var tupleBuilder = RelationTuple.newBuilder()
          .setNamespace(permission.namespace())
          .setObject(permission.object())
          .setRelation(permission.relation())
          .setSubject(subjectBuilder);
      deltaBuilder.setRelationTuple(tupleBuilder);
      builder.addRelationTupleDeltas(deltaBuilder);
    }
    return builder.build();
  }

  @Override
  public Optional<Actor> findActor(String username) {
    return actorRepository.findByUsername(username).map(this::fromEntity);
  }

  @Override
  public User findUser(String username) {
    return userRepository.findByUsername(username).map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  @Override
  public Optional<Actor> addPermissionsToActor(String username, List<PermissionTupleInput> permissions) {
    ketoAuthorizationService.transactRelationship(toRelationTuplesRequest(Action.ACTION_INSERT, permissions));
    return actorRepository.findByUsername(username).map(this::fromEntity);
  }

  @Override
  public Optional<Actor> removePermissionsFromActor(String username, List<PermissionTupleInput> permissions) {
    ketoAuthorizationService.transactRelationship(toRelationTuplesRequest(Action.ACTION_DELETE, permissions));
    return actorRepository.findByUsername(username).map(this::fromEntity);
  }

  @Override
  public List<Role> findRoles() {
    var roles = Arrays.stream(BuiltIns.DEFAULT_ROLES);
    return roles.map(role -> new Role(
        role.getName(), Arrays.stream(role.getPermissions()).map(
        permission -> new Permission(
            permission.getNamespace(),
            permission.getPermission())).toList()
    )).toList();
  }

  @Override
  public List<Permission> findPermissions() {
    return Arrays.stream(BuiltIns.DEFAULT_PERMISSIONS).map(
        permission -> new Permission(
            permission.getNamespace(),
            permission.getPermission())).toList();
  }
}
