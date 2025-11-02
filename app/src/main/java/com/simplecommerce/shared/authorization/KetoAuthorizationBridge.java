package com.simplecommerce.shared.authorization;

import com.simplecommerce.shared.authorization.BasePermissions.Namespaces;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sh.ory.keto.relation_tuples.v1alpha2.RelationTuple;
import sh.ory.keto.relation_tuples.v1alpha2.Subject;
import sh.ory.keto.relation_tuples.v1alpha2.SubjectSet;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta.Action;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;

@Component
@Profile("keto-authz")
class KetoAuthorizationBridge implements AuthorizationBridge {
  private static final String GROUP_NAMESPACE = Namespaces.GROUP_NAMESPACE;
  private static final String MEMBERS_RELATION = "members";
  private static final Logger LOG = LoggerFactory.getLogger(KetoAuthorizationBridge.class);
  private final KetoAuthorizationService keto;

  KetoAuthorizationBridge(KetoAuthorizationService keto) {
    this.keto = keto;
  }

  @Override
  public void addActorsToGroup(String groupId, List<String> actorUsernames) {
    if (actorUsernames.isEmpty()) return;
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var username : actorUsernames) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace(GROUP_NAMESPACE)
              .setObject(groupId)
              .setRelation(MEMBERS_RELATION)
              .setSubject(Subject.newBuilder().setId(username))));
    }
    keto.transactRelationship(builder.build());
    LOG.debug("Added {} actor members to Group:{}", actorUsernames.size(), groupId);
  }

  @Override
  public void addGroupsToGroup(String parentGroupId, List<String> nestedGroupIds) {
    if (nestedGroupIds.isEmpty()) return;
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
    keto.transactRelationship(builder.build());
    LOG.debug("Added {} nested groups to Group:{}", nestedGroupIds.size(), parentGroupId);
  }

  @Override
  public void assignGroupPermissionOnProducts(String groupId, List<String> productIds, String relation) {
    if (productIds.isEmpty()) return;
    var builder = TransactRelationTuplesRequest.newBuilder();
    for (var pid : productIds) {
      builder.addRelationTupleDeltas(RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
          .setRelationTuple(RelationTuple.newBuilder()
              .setNamespace("Product")
              .setObject(pid)
              .setRelation(relation)
              .setSubject(Subject.newBuilder().setSet(SubjectSet.newBuilder()
                  .setNamespace(GROUP_NAMESPACE)
                  .setObject(groupId)
                  .setRelation(MEMBERS_RELATION)))));
    }
    keto.transactRelationship(builder.build());
    LOG.debug("Assigned relation '{}' on {} products via Group:{}#members", relation, productIds.size(), groupId);
  }

  @Override
  public void purgeGroupRelations(String groupId) {
    // No-op for now. Implement when delete group is introduced.
  }
}
