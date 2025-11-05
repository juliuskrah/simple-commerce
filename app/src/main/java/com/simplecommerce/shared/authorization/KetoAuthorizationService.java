package com.simplecommerce.shared.authorization;

import static com.simplecommerce.shared.utils.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.runInScope;

import com.google.protobuf.ByteString;
import com.simplecommerce.shared.types.ProductStatus;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import sh.ory.keto.opl.v1alpha1.CheckRequest;
import sh.ory.keto.opl.v1alpha1.ParseError;
import sh.ory.keto.opl.v1alpha1.SyntaxServiceGrpc.SyntaxServiceBlockingStub;
import sh.ory.keto.read.v1alpha2.CheckServiceGrpc.CheckServiceBlockingStub;
import sh.ory.keto.read.v1alpha2.ListNamespacesRequest;
import sh.ory.keto.read.v1alpha2.Namespace;
import sh.ory.keto.read.v1alpha2.NamespacesServiceGrpc.NamespacesServiceBlockingStub;
import sh.ory.keto.relation_tuples.v1alpha2.RelationTuple;
import sh.ory.keto.relation_tuples.v1alpha2.Subject;
import sh.ory.keto.write.v1alpha2.DeleteRelationTuplesRequest;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;
import sh.ory.keto.write.v1alpha2.WriteServiceGrpc.WriteServiceBlockingStub;

/**
 * Service for interacting with Ory Keto authorization system.
 * Provides methods for checking permissions and managing relationships
 * using the ReBAC (Relationship-Based Access Control) model.
 * 
 * @author julius.krah
 */
@Service("authz")
@Profile("keto-authz")
public class KetoAuthorizationService {
  private static final Logger LOG = LoggerFactory.getLogger(KetoAuthorizationService.class);
  private static final int DEFAULT_MAX_CHECK_DEPTH = 5;
  private final NamespacesServiceBlockingStub namespacesService;
  private final SyntaxServiceBlockingStub syntaxService;
  private final CheckServiceBlockingStub checkService;
  private final WriteServiceBlockingStub writeService;

  public KetoAuthorizationService(NamespacesServiceBlockingStub namespacesService,
      SyntaxServiceBlockingStub syntaxService,
      CheckServiceBlockingStub checkService,
      WriteServiceBlockingStub writeService) {
    this.namespacesService = namespacesService;
    this.syntaxService = syntaxService;
    this.checkService = checkService;
    this.writeService = writeService;
  }

  /**
   * List all namespaces defined in Keto.
   *
   * @return List of namespace names
   */
  public List<String> listNamespaces() {
    LOG.info("Listing all namespaces");
    var namespaceResponse = callInScope(() -> namespacesService.listNamespaces(ListNamespacesRequest.newBuilder().build()));
    return namespaceResponse.getNamespacesList().stream()
        .map(Namespace::getName).toList();
    }

    /**
     * Validate the syntax of an OPL policy resource.
     *
     * @param resource The OPL policy file to validate
     * @return List of ParseError if any syntax errors are found
     */
    public List<ParseError> checkSyntax(Resource resource) {
      LOG.info("Validating OPL syntax: {}", resource);
      var checkResponse = callInScope(() -> syntaxService.check(CheckRequest.newBuilder()
          .setContent(ByteString.readFrom(resource.getInputStream()))
          .build())
      );
      return checkResponse.getParseErrorsList();
    }

    /**
     * Check if a subject has a specific permission on a resource.
     * 
     * @param namespace The namespace (e.g., "Customer", "Actor", "Order")
     * @param object The object identifier
     * @param relation The relation to check (e.g., "edit", "view", "remove")
     * @param subject The subject identifier
     * @return Boolean indicating if the permission is granted
     */
    public boolean checkPermission(String namespace, @Nullable String object, String relation, String subject) {
        LOG.debug("Checking permission: {}:{}#{} for subject {}", namespace, object, relation, subject);
      var checkResponse = callInScope(() -> checkService.check(sh.ory.keto.read.v1alpha2.CheckRequest.newBuilder()
                .setTuple(RelationTuple.newBuilder()
                    .setNamespace(namespace)
                    .setObject(object)
                    .setRelation(relation)
                    .setSubject(Subject.newBuilder().setId(subject)))
                .setMaxDepth(DEFAULT_MAX_CHECK_DEPTH)
          .build())
      );
        return checkResponse.getAllowed();
    }

    public boolean checkPermission(String namespace, String object, String relation, String subject, Object returnObject) {
        var hasPermission = checkPermission(namespace, object, relation, subject);
      return switch (returnObject) {
        case ProductStatus status -> hasPermission || status == ProductStatus.PUBLISHED;
        default -> hasPermission;
      };
    }

  /**
   * Create or delete a relationship tuple within a transaction in Keto.
   *
   * @param transactionRequest The transaction request containing relation tuple deltas
   */
    public void transactRelationship(TransactRelationTuplesRequest transactionRequest) {
        LOG.debug("Creating/Deleting {} relationship tuples within transaction", transactionRequest.getRelationTupleDeltasCount());
      var transactionResponse = callInScope(() -> writeService.transactRelationTuples(transactionRequest));
        LOG.debug("Transaction completed with {} snap-token count", transactionResponse.getSnaptokensCount());
    }

    public void deleteRelationship(DeleteRelationTuplesRequest transactionRequest) {
      LOG.debug("Deleting relationship tuple for query: '{}'", transactionRequest.getRelationQuery());
      runInScope(() -> writeService.deleteRelationTuples(transactionRequest));
      LOG.debug("Relation deleted");
    }

  // Role listing for group will be implemented when Keto read relation listing stubs are available.

}