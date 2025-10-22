package com.simplecommerce.product;

import com.simplecommerce.shared.authorization.BaseRoles;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import com.simplecommerce.shared.types.PermissionTupleInput.SubjectInput;
import com.simplecommerce.shared.types.PermissionTupleInput.SubjectSetInput;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Profile;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import sh.ory.keto.relation_tuples.v1alpha2.RelationQuery;
import sh.ory.keto.relation_tuples.v1alpha2.RelationTuple;
import sh.ory.keto.relation_tuples.v1alpha2.Subject;
import sh.ory.keto.relation_tuples.v1alpha2.SubjectSet;
import sh.ory.keto.write.v1alpha2.DeleteRelationTuplesRequest;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta;
import sh.ory.keto.write.v1alpha2.RelationTupleDelta.Action;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;

/**
 * @author julius.krah
 */
@Component
@Profile("keto-authz")
class ProductEventHandler implements ApplicationEventPublisherAware {

  private static final Logger LOG = LoggerFactory.getLogger(ProductEventHandler.class);
  private static final String PERMISSION_ASSIGNED = "PERMISSION_ASSIGNED";
  private static final String PERMISSION_REVOKED = "PERMISSION_REVOKED";
  private final KetoAuthorizationService ketoAuthorizationService;
  private ApplicationEventPublisher publisher;

  ProductEventHandler(KetoAuthorizationService ketoAuthorizationService) {
    this.ketoAuthorizationService = ketoAuthorizationService;
  }

  private TransactRelationTuplesRequest toRelationTuplesRequest(SubjectInput actor, String relation, String productId) {
    var subjectBuilder = Subject.newBuilder();
    if (Objects.nonNull(actor.subjectId())) {
      subjectBuilder.setId(actor.subjectId());
    } else if (Objects.nonNull(actor.subjectSet())) {
      subjectBuilder.setSet(SubjectSet.newBuilder()
          .setNamespace(actor.subjectSet().namespace())
          .setObject(actor.subjectSet().object())
          .setRelation(actor.subjectSet().relation()));
    }
    return TransactRelationTuplesRequest.newBuilder()
        .addRelationTupleDeltas(
            RelationTupleDelta.newBuilder().setAction(Action.ACTION_INSERT)
                .setRelationTuple(RelationTuple.newBuilder()
                    .setNamespace("Product")
                    .setObject(productId)
                    .setRelation(relation)
                    .setSubject(subjectBuilder))
        )
        .build();
  }

  private void createPermissionForActor(ProductEntity product) {
    var subjectIdOption = product.getCreatedBy();
    subjectIdOption.ifPresent(subjectId -> {
      var permission = toRelationTuplesRequest(new SubjectInput(subjectId, null), "owners", product.getId().toString());
      ketoAuthorizationService.transactRelationship(permission);
      LOG.debug("Assigned owner on Product:{} to subject {}", product.getId(), subjectId);
      AuditApplicationEvent auditEvent = new AuditApplicationEvent(subjectId, PERMISSION_ASSIGNED, Map.of(
          "namespace", "Product",
          "object", product.getId(),
          "relation", "owner"
      ));
      publisher.publishEvent(auditEvent);
    });
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).CREATED")
  void assignActorPermission(ProductEvent event) {
    var source = event.source();
    createPermissionForActor(source);
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).CREATED")
  void assignGroupPermission(ProductEvent event) {
    var source = event.source();
    var permission = toRelationTuplesRequest(new SubjectInput(null,
            new SubjectSetInput("Group", BaseRoles.MERCHANDISER.getName(), "members")),
        "owners", source.getId().toString());
    ketoAuthorizationService.transactRelationship(permission);
    LOG.debug("Assigned owner on Product:{} to group {}", source.getId(), BaseRoles.MERCHANDISER.getName());
    AuditApplicationEvent auditEvent = new AuditApplicationEvent(BaseRoles.MERCHANDISER.getName(), PERMISSION_ASSIGNED, Map.of(
        "namespace", "Product",
        "object", source.getId(),
        "relation", "owner"
    ));
    publisher.publishEvent(auditEvent);
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).DELETED")
  void revokeProductPermission(ProductEvent event) {
    var source = event.source();
    var request = DeleteRelationTuplesRequest.newBuilder()
        .setRelationQuery(RelationQuery.newBuilder().setNamespace("Product").setObject(source.getId().toString())).build();
    ketoAuthorizationService.deleteRelationship(request);
    LOG.debug("Revoked all permissions for Product:{}", source.getId());
    AuditApplicationEvent auditEvent = new AuditApplicationEvent(null, PERMISSION_REVOKED, Map.of(
        "namespace", "Product",
        "object", source.getId()
    ));
    publisher.publishEvent(auditEvent);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }
}
