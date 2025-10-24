package com.simplecommerce.shared.authorization;

import static com.simplecommerce.shared.authorization.BasePermissions.DELETE_PRODUCTS;
import static com.simplecommerce.shared.authorization.BasePermissions.VIEW_PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.JsonFormat;
import com.simplecommerce.config.security.KetoConfiguration;
import com.simplecommerce.shared.authorization.BasePermissions.Namespaces;
import com.simplecommerce.shared.authorization.KetoAuthorizationServiceIT.KetoInnerConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.grpc.autoconfigure.client.GrpcClientAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest;
import sh.ory.keto.write.v1alpha2.TransactRelationTuplesRequest.Builder;

/// Creating and validating Ory Keto authorization service tests. The diagram below illustrates the interaction flow.
///
/// ```mermaid
/// ---
/// title: Namespaces
/// ---
/// flowchart LR
///     actors---> groups
///     groups---> roles
///     actors---> roles
///     roles---> products
///     products---> product_variants
///     product_variants---> orders
///     actors(Actor)
///     products(Product)
///     product_variants(ProductVariant)
///     orders(Order)
///     categories(Category)
///     groups(Group)
///     roles(Role)
/// ```
///
/// @author julius.krah
@Testcontainers
@ActiveProfiles({"test", "keto-authz"})
@Import({KetoConfiguration.class, GrpcClientAutoConfiguration.class, SslAutoConfiguration.class, KetoInnerConfiguration.class})
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = KetoAuthorizationService.class)
class KetoAuthorizationServiceIT {

  private static final int KETO_READ_PORT = 4466;
  private static final int KETO_WRITE_PORT = 4467;
  private static final int KETO_OPL_PORT = 4469;
  @Autowired
  private KetoAuthorizationService ketoAuthorizationService;
  @Container
  static final ComposeContainer COMMERCE_COMPOSE_CONTAINER = new ComposeContainer(
      new File("../compose.yaml"))
      .withEnv("COMPOSE_PROFILES", "oidc-authn,keto-authz")
      .withExposedService("keto", 1, KETO_READ_PORT)
      .withExposedService("keto", 1, KETO_WRITE_PORT)
      .withExposedService("keto", 1, KETO_OPL_PORT);

  @TestConfiguration
  static class KetoInnerConfiguration {

    @Bean
    DynamicPropertyRegistrar ketoProperties() {
      var readAddress = "%s:%d".formatted(
          COMMERCE_COMPOSE_CONTAINER.getServiceHost("keto-1",
              KETO_READ_PORT), COMMERCE_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_READ_PORT));
      var writeAddress = "%s:%d".formatted(
          COMMERCE_COMPOSE_CONTAINER.getServiceHost("keto-1",
              KETO_WRITE_PORT), COMMERCE_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_WRITE_PORT));
      var oplAddress = "%s:%d".formatted(
          COMMERCE_COMPOSE_CONTAINER.getServiceHost("keto-1",
              KETO_OPL_PORT), COMMERCE_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_OPL_PORT));
      return registry -> {
        registry.add("spring.grpc.client.channels.keto-read.address", () -> readAddress);
        registry.add("spring.grpc.client.channels.keto-write.address", () -> writeAddress);
        registry.add("spring.grpc.client.channels.keto-opl.address", () -> oplAddress);
      };
    }
  }

  @BeforeEach
  void confirmRunningServiceAndCreateRelationship() throws IOException {
    assertThat(COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName("keto-1")).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
    var parser = JsonFormat.parser().ignoringUnknownFields();
    var permissionResource = new ClassPathResource("authz/permissions.keto.json");
    Builder relationTuplesBuilder = TransactRelationTuplesRequest.newBuilder();
    parser.merge(new InputStreamReader(permissionResource.getInputStream()), relationTuplesBuilder);
    ketoAuthorizationService.transactRelationship(relationTuplesBuilder.build());
  }

  @AfterEach
  void clearRelationships() throws IOException {
    var parser = JsonFormat.parser().ignoringUnknownFields();
    var permissionResource = new ClassPathResource("authz/remove-permissions.keto.json");
    Builder relationTuplesBuilder = TransactRelationTuplesRequest.newBuilder();
    parser.merge(new InputStreamReader(permissionResource.getInputStream()), relationTuplesBuilder);
  }

  @Test
  void testListNamespaces() {
    var namespaces = ketoAuthorizationService.listNamespaces();
    assertThat(namespaces).isNotNull()
        .isNotEmpty()
        .containsOnly(
            Namespaces.ACTOR_NAMESPACE,
            Namespaces.GROUP_NAMESPACE,
            Namespaces.PRODUCT_NAMESPACE,
            Namespaces.PRODUCT_VARIANT_NAMESPACE,
            Namespaces.ORDER_NAMESPACE,
            Namespaces.CATEGORY_NAMESPACE,
            Namespaces.ROLE_NAMESPACE);
  }

  @Test
  void testCheckSyntax() throws IOException {
    var parseResult = ketoAuthorizationService.checkSyntax(new PathResource("../keto/permissions.keto.ts"));
    assertThat(parseResult).isNotNull()
        .isEmpty();
  }

  @Nested
  class CheckCategoryPermissions {
    private static final String ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY = "neo";
    private static final String ACTOR_HAS_WRITE_ON_ANCESTOR_CATEGORY = "morpheus";
    private static final String ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP = "trinity";

    @Test
    @DisplayName("Actor has read permission on ancestor category")
    void testHasReadPermissionAncestorCategory() {
      var categoryMedia = "daba3cfa-5a44-44a9-827e-b100094d06a6"; // Media
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryMedia, "view", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
      var hasPermissionViaWrite = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryMedia, "view", ACTOR_HAS_WRITE_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermissionViaWrite).isTrue();
    }

    @Test
    @DisplayName("Actor has write permission on ancestor category")
    void testHasWritePermissionAncestorCategory() {
      var categoryMedia = "daba3cfa-5a44-44a9-827e-b100094d06a6"; // Media
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryMedia, "edit", ACTOR_HAS_WRITE_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Actor has no read permission on ancestor category")
    void testHasNoReadPermissionCategory() {
      var categoryMedia = "daba3cfa-5a44-44a9-827e-b100094d06a6"; // Media
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryMedia, "view", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Actor has no write permission on ancestor category")
    void testHasNoWritePermissionCategory() {
      var categoryMedia = "daba3cfa-5a44-44a9-827e-b100094d06a6"; // Media
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryMedia, "edit", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isFalse();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryMedia, "edit", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermissionViaGroup).isFalse();
    }

    @Test
    @DisplayName("Actor has read permission on parent category")
    void testHasReadPermissionParentCategory() {
      var categoryVideos = "393ce9a2-c562-4dc1-8d67-2d28ab6e2776"; // Media > Videos
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryVideos, "view", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryVideos, "view", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermissionViaGroup).isTrue();
      var hasPermissionViaWrite = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryVideos, "view", ACTOR_HAS_WRITE_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermissionViaWrite).isTrue();
    }

    @Test
    @DisplayName("Actor has no write permission on parent category")
    void testHasNoWritePermissionParentCategory() {
      var categoryVideos = "393ce9a2-c562-4dc1-8d67-2d28ab6e2776"; // Media > Videos
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryVideos, "edit", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isFalse();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryVideos, "edit", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermissionViaGroup).isFalse();
    }

    @Test
    @DisplayName("Actor has read permission on child category")
    void testHasReadPermissionChildCategory() {
      var categoryDocumentaries = "64b66eab-e1d0-4b00-ac64-a8cf1daebcc3"; // Media > Videos > Digital Downloads
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryDocumentaries, "view", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryDocumentaries, "view", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermissionViaGroup).isTrue();
      var hasPermissionViaWrite = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryDocumentaries, "view", ACTOR_HAS_WRITE_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermissionViaWrite).isTrue();
    }

    @Test
    @DisplayName("Actor has no write permission on child category")
    void testHasNoWritePermissionChildCategory() {
      var categoryDocumentaries = "64b66eab-e1d0-4b00-ac64-a8cf1daebcc3"; // Media > Videos > Digital Downloads
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryDocumentaries, "edit", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isFalse();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission(Namespaces.CATEGORY_NAMESPACE, categoryDocumentaries, "edit", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermissionViaGroup).isFalse();
    }
  }

  @Nested
  class CheckProductPermissions {

    private static final String ACTOR_HAS_READ_ON_ALL_PRODUCTS = "tank";
    private static final String ACTOR_HAS_OWNER_ON_PRODUCT = "neo";

    @Test
    @DisplayName("Actor has read permission on all products")
    void testHasReadPermissionOnAllProducts() {
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.PRODUCT_NAMESPACE, "__ALL__", VIEW_PRODUCTS.getPermission(), ACTOR_HAS_READ_ON_ALL_PRODUCTS);
      assertThat(hasPermission).isTrue();
    }

    @DisplayName("Actor has delete/edit/view permission on specific product")
    @ParameterizedTest
    @ValueSource(strings = {"delete", "edit", "view"})
    void testOwnerHasPermissionsOnProduct(String permission) {
      var productId = "eff5dfb9-3caf-40ff-9ed9-55045e2022be"; // The Matrix
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.PRODUCT_NAMESPACE, productId, permission, ACTOR_HAS_OWNER_ON_PRODUCT);
      assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Actor has no delete permission on specific product")
    void testNonOwnerHasNoDeletePermissionOnProduct() {
      var productId = "eff5dfb9-3caf-40ff-9ed9-55045e2022be"; // The Matrix
      var hasPermission = ketoAuthorizationService.checkPermission(Namespaces.PRODUCT_NAMESPACE, productId, DELETE_PRODUCTS.getPermission(), ACTOR_HAS_READ_ON_ALL_PRODUCTS);
      assertThat(hasPermission).isFalse();
    }
  }
}
