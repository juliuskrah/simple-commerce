package com.simplecommerce.shared.authorization;

import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.GROUP_NAMESPACE;
import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.PRODUCT_NAMESPACE;
import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.ROLE_NAMESPACE;

import com.simplecommerce.shared.utils.SecurityUtils;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sh.ory.keto.relation_tuples.v1alpha2.RelationQuery;
import sh.ory.keto.relation_tuples.v1alpha2.RelationTuple;
import sh.ory.keto.relation_tuples.v1alpha2.Subject;
import sh.ory.keto.relation_tuples.v1alpha2.SubjectSet;
import sh.ory.keto.write.v1alpha2.DeleteRelationTuplesRequest;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta.Action;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;

@Component
@Profile("keto-authz")
class KetoAuthorizationBridge implements AuthorizationBridge, ApplicationEventPublisherAware {

  private static final String PERMISSION_ASSIGNED = "PERMISSION_ASSIGNED";
  private static final String PERMISSION_REVOKED = "PERMISSION_REVOKED";
  private static final String ASSIGNEES_RELATION = "assignees";
  private static final String MEMBERS_RELATION = "members";
  private static final String NAMESPACE_KEY = "namespace";
  private static final String RELATION_KEY = "relation";
  private static final String SUBJECT_KEY = "subject";
  private static final String OBJECT_KEY = "object";
  private static final Logger LOG = LoggerFactory.getLogger(KetoAuthorizationBridge.class);
  private final KetoAuthorizationService ketoService;
  private ApplicationEventPublisher publisher;

  KetoAuthorizationBridge(KetoAuthorizationService ketoService) {
    this.ketoService = ketoService;
  }

  @NonNull
  private static String getPrincipal() {
    return SecurityUtils.getCurrentUserLogin().orElse("system");
  }

  private void publishAuditEvent(String eventType, String namespace, String object, String relation, String subject) {
    AuditApplicationEvent auditEvent = new AuditApplicationEvent(getPrincipal(), eventType, Map.of(
        NAMESPACE_KEY, namespace,
        OBJECT_KEY, object,
        RELATION_KEY, relation,
        SUBJECT_KEY, subject
    ));
    publisher.publishEvent(auditEvent);
  }

  private void publishAuditEvent(String namespace, String object) {
    AuditApplicationEvent auditEvent = new AuditApplicationEvent(getPrincipal(), PERMISSION_REVOKED, Map.of(
        NAMESPACE_KEY, namespace,
        OBJECT_KEY, object
    ));
    publisher.publishEvent(auditEvent);
  }

  @Override
  public void addActorsToGroup(String groupId, List<String> actorUsernames) {
    if (actorUsernames.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var username : actorUsernames) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(GROUP_NAMESPACE)
              .setObject(groupId)
              .setRelation(MEMBERS_RELATION)
              .setSubject(Subject.newBuilder().setId(username))));
    }
    ketoService.transactRelationship(builder.build());
    for (var username : actorUsernames) {
      publishAuditEvent(PERMISSION_ASSIGNED, GROUP_NAMESPACE, groupId, MEMBERS_RELATION, username);
    }
    LOG.debug("Added {} actor members to Group:{}", actorUsernames.size(), groupId);
  }


  @Override
  public void addGroupsToGroup(String parentGroupId, List<String> nestedGroupIds) {
    if (nestedGroupIds.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var child : nestedGroupIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(GROUP_NAMESPACE)
              .setObject(parentGroupId)
              .setRelation(MEMBERS_RELATION)
              .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
                  .setNamespace(GROUP_NAMESPACE).setObject(child)
              ))));
    }
    ketoService.transactRelationship(builder.build());
    for (var child : nestedGroupIds) {
      publishAuditEvent(PERMISSION_ASSIGNED, GROUP_NAMESPACE, parentGroupId, MEMBERS_RELATION, GROUP_NAMESPACE + ":" + child);
    }
    LOG.debug("Added {} nested groups to Group:{}", nestedGroupIds.size(), parentGroupId);
  }

  @Override
  public void assignGroupPermissionOnProducts(String groupId, List<String> productIds, String relation) {
    if (productIds.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var pid : productIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(PRODUCT_NAMESPACE)
              .setObject(pid)
              .setRelation(relation)
              .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
                  .setNamespace(GROUP_NAMESPACE)
                  .setObject(groupId)
                  .setRelation(MEMBERS_RELATION)))));
    }
    ketoService.transactRelationship(builder.build());
    for (var pid : productIds) {
      publishAuditEvent(PERMISSION_ASSIGNED, PRODUCT_NAMESPACE, pid, relation, GROUP_NAMESPACE + ":" + groupId + "#" + MEMBERS_RELATION);
    }
    LOG.debug("Assigned relation '{}' on {} products via Group:{}#members", relation, productIds.size(), groupId);
  }

  @Override
  public void assignActorPermissionOnProducts(String username, List<String> productIds, String relation) {
    if (productIds.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var pid : productIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(PRODUCT_NAMESPACE)
              .setObject(pid)
              .setRelation(relation)
              .setSubject(Subject.newBuilder().setId(username))));
    }
    ketoService.transactRelationship(builder.build());
    for (var pid : productIds) {
      publishAuditEvent(PERMISSION_ASSIGNED, PRODUCT_NAMESPACE, pid, relation, username);
    }
    LOG.debug("Assigned relation '{}' on {} products to Actor:{}", relation, productIds.size(), username);
  }

  @Override
  public void assignRolePermissionOnProducts(String role, List<String> productIds, String relation) {
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var pid : productIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(PRODUCT_NAMESPACE)
              .setObject(pid)
              .setRelation(relation)
              .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
                  .setNamespace(ROLE_NAMESPACE)
                  .setObject(role)
                  .setRelation(ASSIGNEES_RELATION)))));
    }
    ketoService.transactRelationship(builder.build());
    for (var pid : productIds) {
      publishAuditEvent(PERMISSION_ASSIGNED, PRODUCT_NAMESPACE, pid, relation, ROLE_NAMESPACE + ":" + role + "#" + ASSIGNEES_RELATION);
    }
    LOG.debug("Assigned relation '{}' on {} products via Role:{}#assignees", relation, productIds.size(), role);
  }

  @Override
  public void assignRolesToGroup(String groupId, List<String> roles) {
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var role : roles) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder()
          .setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(ROLE_NAMESPACE)
              .setObject(role)
              .setRelation(ASSIGNEES_RELATION)
              .setSubject(Subject.newBuilder()
                  .setSet(SubjectSet.newBuilder()
                      .setNamespace(GROUP_NAMESPACE)
                      .setObject(groupId)
                      .setRelation(MEMBERS_RELATION))

              )
          ));
    }
    ketoService.transactRelationship(builder.build());
    for (var role : roles) {
      publishAuditEvent(PERMISSION_ASSIGNED, ROLE_NAMESPACE, role, ASSIGNEES_RELATION, GROUP_NAMESPACE + ":" + groupId + "#" + MEMBERS_RELATION);
    }
    LOG.debug("Assigned relation '{}' on {} roles to Group:{}#members", ASSIGNEES_RELATION, roles.size(), groupId);
  }

  @Override
  public void assignRolesToActor(String actorUsername, List<String> roles) {
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var role : roles) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder()
          .setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(ROLE_NAMESPACE)
              .setObject(role)
              .setRelation(ASSIGNEES_RELATION)
              .setSubject(Subject.newBuilder()
                  .setId(actorUsername))
          ));
    }
    ketoService.transactRelationship(builder.build());
    for (var role : roles) {
      publishAuditEvent(PERMISSION_ASSIGNED, ROLE_NAMESPACE, role, ASSIGNEES_RELATION, actorUsername);
    }
    LOG.debug("Assigned relation '{}' on {} roles to Actor:{}", ASSIGNEES_RELATION, roles.size(), actorUsername);
  }

  @Override
  public void revokeGroupPermissionOnProducts(String groupId, List<String> productIds, String relation) {
    if (productIds.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var pid : productIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_DELETE)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(PRODUCT_NAMESPACE)
              .setObject(pid)
              .setRelation(relation)
              .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
                  .setNamespace(GROUP_NAMESPACE)
                  .setObject(groupId)
                  .setRelation(MEMBERS_RELATION)))));
    }
    ketoService.transactRelationship(builder.build());
    for (var pid : productIds) {
      publishAuditEvent(PERMISSION_REVOKED, PRODUCT_NAMESPACE, pid, relation, GROUP_NAMESPACE + ":" + groupId + "#" + MEMBERS_RELATION);
    }
    LOG.debug("Revoked '{}' from {} products via Group:{}#members", relation, productIds.size(), groupId);
  }

  @Override
  public void removeActorsFromGroup(String groupId, List<String> actorUsernames) {
    if (actorUsernames.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var username : actorUsernames) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_DELETE)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(GROUP_NAMESPACE)
              .setObject(groupId)
              .setRelation(MEMBERS_RELATION)
              .setSubject(Subject.newBuilder().setId(username))));
    }
    ketoService.transactRelationship(builder.build());
    for (var username : actorUsernames) {
      publishAuditEvent(PERMISSION_REVOKED, GROUP_NAMESPACE, groupId, MEMBERS_RELATION, username);
    }
    LOG.debug("Removed {} actors from Group:{}", actorUsernames.size(), groupId);
  }

  @Override
  public void removeGroupsFromGroup(String parentGroupId, List<String> nestedGroupIds) {
    if (nestedGroupIds.isEmpty()) {
      return;
    }
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var child : nestedGroupIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_DELETE)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(GROUP_NAMESPACE)
              .setObject(parentGroupId)
              .setRelation(MEMBERS_RELATION)
              .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
                  .setNamespace(GROUP_NAMESPACE).setObject(child)
              ))));
    }
    ketoService.transactRelationship(builder.build());
    for (var child : nestedGroupIds) {
      publishAuditEvent(PERMISSION_REVOKED, GROUP_NAMESPACE, parentGroupId, MEMBERS_RELATION, GROUP_NAMESPACE + ":" + child);
    }
    LOG.debug("Removed {} nested groups from Group:{}", nestedGroupIds.size(), parentGroupId);
  }

  @Override
  public void purgeGroupRelations(String groupId) {
    var request = DeleteRelationTuplesRequest.newBuilder()
        .setRelationQuery(RelationQuery.newBuilder().setNamespace(GROUP_NAMESPACE).setObject(groupId)).build();
    ketoService.deleteRelationship(request);
    publishAuditEvent(GROUP_NAMESPACE, groupId);
    LOG.debug("Purged all relations for Group:{}", groupId);
  }

  @Override
  public void purgeProductRelations(String productId) {
    var request = DeleteRelationTuplesRequest.newBuilder()
        .setRelationQuery(RelationQuery.newBuilder().setNamespace(PRODUCT_NAMESPACE).setObject(productId)).build();
    ketoService.deleteRelationship(request);
    publishAuditEvent(PRODUCT_NAMESPACE, productId);
    LOG.debug("Purged all relations for Product:{}", productId);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }
}
