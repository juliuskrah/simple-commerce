package com.simplecommerce.seed;

import static com.simplecommerce.shared.authorization.BaseRoles.ADMINISTRATOR;
import static com.simplecommerce.shared.authorization.BaseRoles.MERCHANDISER;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.Types;
import graphql.execution.ExecutionId;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.ExecutionGraphQlResponse;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author julius.krah
 */
@Service
@ConditionalOnProperty(prefix = "simple-commerce.seeder", name = "enabled", havingValue = "true")
public class SeedingService {

  private static final Logger LOG = LoggerFactory.getLogger(SeedingService.class);
  private final ExecutionGraphQlService executionService;
  private final ResourceLoader resourceLoader;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;
  @Value("${simple-commerce.seeder.directory-prefix}")
  private String directoryPrefix;

  public SeedingService(ExecutionGraphQlService executionService, ResourceLoader resourceLoader, ObjectMapper objectMapper, RestClient restClient) {
    this.executionService = executionService;
    this.resourceLoader = resourceLoader;
    this.objectMapper = objectMapper;
    this.restClient = restClient;
  }

  private void seedProductMedia(Flux<Product> products) throws IOException {
    var productMedia = loadResource(directoryPrefix + "product_media.json");
    var productMediaVariables = objectMapper.readValue(productMedia.getInputStream(),
        new TypeReference<Set<Map<String, Object>>>() {
        });
    Flux<Map<String, Object>> mediaVariables = products.map(product ->
      productMediaVariables.stream()
          .filter(media -> media.get("productId") == product.syntheticId())
          .map(media -> {media.put("id", product.naturalId()); return media;})
          .collect(Collectors.toSet())
    ).flatMap(Flux::fromIterable);
    uploadMedia(mediaVariables).blockLast();
  }

  private Flux<Map<String, Object>> uploadMedia(Flux<Map<String, Object>> mediaVariables) {
    return Flux.from(mediaVariables)
        .flatMap(variable -> {
          @SuppressWarnings("unchecked")
          var images = (List<String>) variable.get("mediaLocations");
          return Flux.fromIterable(images)
              .flatMap(this::createStaged)
              .publishOn(Schedulers.boundedElastic())
              .map(staged -> uploadMedia(staged, variable));
        }).log();
  }

  private Flux<Product> seedProduct() throws IOException {
    @Language("GraphQL")
    var makeProduct = """
        mutation makeProduct($input: ProductInput!) {
          addProduct(input: $input) {
            id
          }
        }
        """;

    var products = loadResource(directoryPrefix + "products.json");
    var productVariables = objectMapper.readValue(products.getInputStream(),
        new TypeReference<Set<Map<String, Object>>>(){});

    return Flux.fromIterable(productVariables)
        .flatMap(variable -> {
          var product = new Product((String)variable.get("id"), null);
          removeId(variable, Set.of("id"));
          var result = executionService.execute(new DefaultExecutionGraphQlRequest(
                makeProduct, null, Map.of("input", variable), null, id(), null));
          return result.map(response -> {
            String nodeId = response.field("addProduct.id").getValue();
            return product.withNaturalId(nodeId);
          });
        })
        .subscribeOn(Schedulers.boundedElastic())
        .log();
  }

  private void seedProductVariants(Flux<Product> products) {
    @Language("GraphQL")
    var addVariant = """
        mutation addVariant($productId: ID!, $input: ProductVariantInput!) {
          addProductVariant(productId: $productId, input: $input) {
            id
            sku
            title
          }
        }
        """;

    // Create additional variants for the first few products for demo purposes
    // Process sequentially to avoid connection pool exhaustion
    products.take(3)
        .filter(product -> product.naturalId() != null) // Only process successfully created products
        .flatMap(product -> {
      // Create 2 additional variants per product
      return Flux.range(1, 2).map(i -> {
        Map<String, Object> variantInput = Map.of(
            "sku", product.syntheticId() + "-variant-" + i,
            "title", "Variant " + i,
            "price", Map.of(
                "amount", String.valueOf(9.99 + (i * 5.00)), // 14.99, 19.99
                "currency", "USD"
            )
        );
        Map<String, Object> variables = Map.of(
            "productId", product.naturalId(),
            "input", variantInput
        );
        return executionService.execute(new DefaultExecutionGraphQlRequest(
            addVariant, null, variables, null, id(), null));
      });
    }, 1) // Process one at a time
        .flatMap(result -> result)
        .doOnNext(response -> {
          try {
            var sku = response.field("addProductVariant.sku").getValue();
            LOG.info("Created variant: {}", String.valueOf(sku));
          } catch (Exception e) {
            LOG.warn("Could not extract variant SKU from response", e);
          }
        })
        .doOnError(error -> LOG.error("Failed to create variant", error))
        .onErrorContinue((error, item) -> LOG.error("Skipping variant creation due to error", error))
        .blockLast();
  }

  private void removeId(Map<String, Object> products, Set<String> keysToRemove) {
    products.keySet().removeAll(keysToRemove);
  }

  private String id() {
    return ExecutionId.generate().toString();
  }

  private Mono<Map<String, Object>> createStaged(String filePath) {
    @Language("GraphQL")
    var stageUpload = """
        mutation stageUpload($input: StagedUploadInput!) {
          stagedUpload(input: $input) {
            presignedUrl
            resourceUrl
            contentType
          }
        }
        """;
    var fileName = Paths.get(filePath).getFileName().toString();
    Map<String, Object> input = Map.of("input", Map.of("filename", fileName));
    var stagedUpload = new DefaultExecutionGraphQlRequest(
        stageUpload, null, input, null, id(), null);
    return executionService.execute(stagedUpload).map(response -> {
      response.field("addProduct.id").getValue();
      String presignedUrl = response.field("stagedUpload.presignedUrl").getValue();
      String resourceUrl = response.field("stagedUpload.resourceUrl").getValue();
      String contentType = response.field("stagedUpload.contentType").getValue();
      return Map.of("presignedUrl", presignedUrl, "resourceUrl", resourceUrl,
          "contentType", contentType, "mediaPath", filePath);
    });
  }

  private Map<String, Object> uploadMedia(
      Map<String, Object> stagedMedia, Map<String, Object> variable) {
    var mediaPath = (String) stagedMedia.get("mediaPath");
    var contentType = (String) stagedMedia.get("contentType");
    var resource = resourceLoader.getResource(directoryPrefix + mediaPath);
    try {
      var response = restClient.put().uri(URI.create((String) stagedMedia.get("presignedUrl")))
          .header(HttpHeaders.CONTENT_TYPE, contentType)
          .body(resource)
          .retrieve().toEntity(String.class);
      LOG.info("Upload status: {}", response.getStatusCode());
    } catch (Exception e) {
      LOG.info("Failed to upload media [{}] to {}", mediaPath, stagedMedia.get("presignedUrl"), e);
    }
    return Map.of(
        "file", Map.of(
            "contentType", contentType,
            "resourceUrl", stagedMedia.get("resourceUrl")),
        "id", variable.get("id"));
  }

  private Resource loadResource(String path) {
    return resourceLoader.getResource(path);
  }

  public void seed() throws IOException {
    var products = seedProduct();
    seedProductMedia(products);
    seedProductVariants(products);
  }

  @NonNull
  private Mono<String> createdGroup(String groupName) {
    @Language("GraphQL") var createGroup = """
        mutation createGroup($name: String!, $description: JsonString) {
            addGroup(name: $name, description: $description) {
                id
            }
        }
        """;
    var variables = Map.<String, Object>of("name", groupName);
    return executionService.execute(new DefaultExecutionGraphQlRequest(
            createGroup, null, variables, null, id(), null))
        .mapNotNull(response -> response.field("addGroup.id").getValue());
  }

  private Mono<ExecutionGraphQlResponse> addMembersToGroup(String groupId, GroupSubject subject) {
    var subjectMap = new HashMap<String, Object>();
    if (subject.actors != null) {
      subjectMap.put("actors", subject.actors);
    } else if (subject.groups != null) {
      subjectMap.put("groups", subject.groups);
    }
    var variables = Map.<String, Object>of(
        "groupId", groupId,
        "subject", subjectMap
    );

    @Language("GraphQL") var addMembersToGroup = """
        mutation addMembersToGroup($groupId: ID!, $subject: GroupMemberInput!) {
            addMembersToGroup(groupId: $groupId, members: $subject) {
                ... on User {
                    __typename
                    id
                    username
                }
                ... on Group {
                    __typename
                    id
                    name
                }
            }
        }
        """;
    return executionService.execute(new DefaultExecutionGraphQlRequest(
        addMembersToGroup, null, variables, null, id(), null));
  }

  Mono<ExecutionGraphQlResponse> assignProductPermissionToSubject(String role, BasePermissions permission, List<String> productIds) {
    LOG.debug("Assigning permission:{} to role:{} for products:{}", permission.getPermission(), role, productIds);
    @Language("GraphQL") var assignPermissionMutation = """
        mutation assignPermission($input: ProductPermissionForSubjectInput!) {
            assignProductPermissionToSubject(input: $input) {
                    __typename
                    ... on Role {
                        name
                    }
                }
        }
        """;
    return executionService.execute(new DefaultExecutionGraphQlRequest(
            assignPermissionMutation, null, Map.of("input", Map.of(
            "permission", permission,
            "productIds", productIds,
            "subject", Map.of("role", role)
        )), null, id(), null))
        .doOnNext(response -> LOG.info("Product permission assignment result:{}", response.getExecutionResult()));
  }

  private Mono<ExecutionGraphQlResponse> assignRolesToSubject(List<@NonNull String> roles, RoleSubject subject) {
    var subjectMap = new HashMap<String, String>();
    if (subject.actor != null) {
      subjectMap.put("actor", subject.actor);
    } else if (subject.group != null) {
      subjectMap.put("group", subject.group);
    }
    var variables = Map.of(
        "roles", roles,
        "subject", subjectMap
    );
    @Language("GraphQL") var addRolesForSubject = """
        mutation addRolesForSubject($roles: [String!]!, $subject: RoleAssigneeInput!) {
            addAssigneesToRoles(roles: $roles, assignees: $subject) {
                ... on User {
                    __typename
                    id
                    username
                }
                ... on Group {
                __typename
                    id
                    name
                }
            }
        }
        """;
    return executionService.execute(new DefaultExecutionGraphQlRequest(
        addRolesForSubject, null, variables, null, id(), null));
  }

  @EventListener(ApplicationReadyEvent.class)
  void assignAdministratorRoleToOwnerGroup() {
    var roles = List.of(ADMINISTRATOR.getName());
    var storeOwner = "simple_commerce";
    createdGroup("Owners").flatMap(groupId -> {
          LOG.info("Assigning Store owner roles:{} to group:{}", roles, groupId);
          return assignRolesToSubject(roles, new RoleSubject(null, groupId))
              .doOnNext(response -> LOG.info("Store owner assignment result:{}", response.getExecutionResult()))
              .doOnError(throwable -> LOG.error("Error assigning owner roles", throwable))
              .mapNotNull(response -> response.field("addAssigneesToRoles.id").<String>getValue());
        }).flatMap(groupId -> addMembersToGroup(groupId, new GroupSubject(List.of(storeOwner), null))
            .doOnNext(response -> LOG.info("Store owner group membership result:{}", response.getExecutionResult()))
            .doOnError(throwable -> LOG.error("Error adding owner to group", throwable)))
        .block();
  }

  @EventListener(ApplicationReadyEvent.class)
  void assignMerchandiserRoleToMerchandiseGroup() {
    var roles = List.of(MERCHANDISER.getName());
    var staff = "trinity";
    createdGroup("Merchandising Operations").flatMap(groupId -> {
      LOG.info("Assigning Staff roles:{} to group:{}", roles, groupId);
          return assignRolesToSubject(roles, new RoleSubject(null, groupId))
              .doOnNext(response -> LOG.info("Staff assignment result:{}", response.getExecutionResult()))
              .doOnError(throwable -> LOG.error("Error assigning staff roles", throwable))
              .mapNotNull(response -> response.field("addAssigneesToRoles.id").<String>getValue());
        }).flatMap(groupId -> addMembersToGroup(groupId, new GroupSubject(List.of(staff), null))
            .doOnNext(response -> LOG.info("Staff group membership result:{}", response.getExecutionResult()))
            .doOnError(throwable -> LOG.error("Error adding staff to group", throwable)))
        .block();
  }

  @EventListener(ApplicationReadyEvent.class)
  void assignPermissionsToRole() {
    // Assign basic product permissions to roles (Administrator, Merchandiser)
    // 1. list products   (__LIST__)
    // 2. create products (__CREATE__)
    var listProduct = new GlobalId(Types.NODE_PRODUCT, "__LIST__").encode();
    var createProduct = new GlobalId(Types.NODE_PRODUCT, "__CREATE__").encode();
    Flux.merge(
        assignProductPermissionToSubject(ADMINISTRATOR.getName(), BasePermissions.VIEW_PRODUCTS, List.of(listProduct)),
        assignProductPermissionToSubject(ADMINISTRATOR.getName(), BasePermissions.CREATE_AND_EDIT_PRODUCTS, List.of(createProduct)),
        assignProductPermissionToSubject(MERCHANDISER.getName(), BasePermissions.VIEW_PRODUCTS, List.of(listProduct)),
        assignProductPermissionToSubject(MERCHANDISER.getName(), BasePermissions.CREATE_AND_EDIT_PRODUCTS, List.of(createProduct))
    ).blockLast();
  }

  record Product(String syntheticId, String naturalId) {
    Product withNaturalId(String naturalId) {
      return new Product(syntheticId, naturalId);
    }
  }

  record RoleSubject(@Nullable String actor, @Nullable String group) {

  }

  record GroupSubject(@Nullable List<@NonNull String> actors, @Nullable List<@NonNull String> groups) {

  }
}
