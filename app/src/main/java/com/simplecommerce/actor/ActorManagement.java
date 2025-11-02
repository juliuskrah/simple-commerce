package com.simplecommerce.actor;

import static com.simplecommerce.actor.user.UserManagement.getUser;
import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.ROLE_NAMESPACE;

import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.security.aspects.Permit;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.authorization.BuiltIns;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
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
  private static final String ASSIGNEES_RELATION = "assignees";

  public void setActorRepository(ObjectFactory<Actors> actorRepository) {
    this.actorRepository = actorRepository.getObject();
  }

  public void setKetoAuthorizationService(ObjectFactory<KetoAuthorizationService> ketoAuthorizationService) {
    this.ketoAuthorizationService = ketoAuthorizationService.getObject();
  }

  private Actors actorRepository;
  private KetoAuthorizationService ketoAuthorizationService;

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

  private TransactRelationTuplesRequest toRelationTuplesRequest(List<String> roles, SubjectInput subject) {
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var role : roles) {
      if (Objects.nonNull(subject.group())) {
        var gid = Objects.requireNonNull(subject.group());
        builder.addRelationTupleDeltas(assignRoleToGroup(role, gid));
      } else if (Objects.nonNull(subject.actor())) {
        var username = Objects.requireNonNull(subject.actor());
        builder.addRelationTupleDeltas(assignRoleToActor(role, username));
      }
    }
    return builder.build();
  }

  private RelationTupleDelta.Builder assignRoleToGroup(@NonNull String role, @NonNull String groupId) {
    return RelationTupleDelta.newBuilder()
        .setAction(Action.ACTION_INSERT)
        .setRelationTuple(RelationTuple.newBuilder()
            .setNamespace("Role")
            .setObject(role)
            .setRelation(ASSIGNEES_RELATION)
            .setSubject(Subject.newBuilder()
                .setSet(SubjectSet.newBuilder()
                    .setNamespace("Group")
                    .setObject(groupId)
                    .setRelation("members"))
            )
        );
  }

  private RelationTupleDelta.Builder assignRoleToActor(@NonNull String role, @NonNull String username) {
    return RelationTupleDelta.newBuilder()
        .setAction(Action.ACTION_INSERT)
        .setRelationTuple(RelationTuple.newBuilder()
            .setNamespace("Role")
            .setObject(role)
            .setRelation(ASSIGNEES_RELATION)
            .setSubject(Subject.newBuilder()
                .setId(username)
            )
        );
  }

  private RelationTupleDelta.Builder revokeRoleFromGroup(@NonNull String role, @NonNull String groupId) {
  return RelationTupleDelta.newBuilder()
    .setAction(Action.ACTION_DELETE)
    .setRelationTuple(RelationTuple.newBuilder()
      .setNamespace("Role")
      .setObject(role)
            .setRelation(ASSIGNEES_RELATION)
      .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
        .setNamespace("Group")
        .setObject(groupId)
        .setRelation("members")))
    );
  }

  private RelationTupleDelta.Builder revokeRoleFromActor(@NonNull String role, @NonNull String username) {
  return RelationTupleDelta.newBuilder()
    .setAction(Action.ACTION_DELETE)
    .setRelationTuple(RelationTuple.newBuilder()
      .setNamespace("Role")
      .setObject(role)
            .setRelation(ASSIGNEES_RELATION)
      .setSubject(Subject.newBuilder().setId(username))
    );
  }

  @Override
  public Optional<Actor> findActor(@NonNull String username) {
    return actorRepository.findByUsername(username).map(this::fromEntity);
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public PermissionAssignmentPayload revokeRolesFromSubject(List<String> roles, SubjectInput subject) {
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var role : roles) {
      if (Objects.nonNull(subject.group())) {
        builder.addRelationTupleDeltas(revokeRoleFromGroup(role, Objects.requireNonNull(subject.group())));
      } else if (Objects.nonNull(subject.actor())) {
        builder.addRelationTupleDeltas(revokeRoleFromActor(role, Objects.requireNonNull(subject.actor())));
      }
    }
    ketoAuthorizationService.transactRelationship(builder.build());
    return new PermissionAssignmentPayload(null, null, null);
  }

  @Permit(namespace = ROLE_NAMESPACE, object = "'Administrator'", relation = "assignees")
  @Override
  public PermissionAssignmentPayload addRolesToSubject(List<String> roles, SubjectInput subject) {
    if (Objects.nonNull(subject.actor())) {
      var username = Objects.requireNonNull(subject.actor());
      return findActor(username).map(actor -> {
            ketoAuthorizationService.transactRelationship(toRelationTuplesRequest(roles, subject));
            return new PermissionAssignmentPayload(actor, null, null);
          }
      ).orElse(new PermissionAssignmentPayload(null, null, null));
    } else if (Objects.nonNull(subject.group())) {
      // For group subjects we still write Role:<role>#assignees@(Group:<gid>#members)
      // Add permission and return group in `PermissionAssignmentPayload` when group with id exists
      ketoAuthorizationService.transactRelationship(toRelationTuplesRequest(roles, subject));
      return new PermissionAssignmentPayload(null, null, null);
    } else {
      return new PermissionAssignmentPayload(null, null, null);
    }
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
