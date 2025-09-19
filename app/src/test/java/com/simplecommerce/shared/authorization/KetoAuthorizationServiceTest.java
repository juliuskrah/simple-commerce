package com.simplecommerce.shared.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.JsonFormat;
import com.simplecommerce.config.security.KetoConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.grpc.autoconfigure.client.GrpcClientAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
///     groups ~~~ actors
///     actors ~~~ products ~~~ product_variants product_variants ~~~ orders ~~~ categories actors(Actor) products(Product) product_variants(ProductVariant) orders(Order) categories(Category)
/// groups(Group)
///```
///
/// @author julius.krah
@Testcontainers
//@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles({"test", "keto-authz"})
@Import({KetoConfiguration.class, GrpcClientAutoConfiguration.class, SslAutoConfiguration.class })
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = KetoAuthorizationService.class)
class KetoAuthorizationServiceTest {

  static final int KETO_READ_PORT = 4466;
  static final int KETO_WRITE_PORT = 4467;
  static final int KETO_OPL_PORT = 4469;
  @Container
  static final ComposeContainer KETO_COMPOSE_CONTAINER = new ComposeContainer(
      new File("../compose.yaml")
  )
      .withOptions("--profile", "keto-authz")
      .withExposedService("keto", 1, KETO_READ_PORT)
      .withExposedService("keto", 1, KETO_WRITE_PORT)
      .withExposedService("keto", 1, KETO_OPL_PORT);

  @Autowired
  KetoAuthorizationService ketoAuthorizationService;

  @DynamicPropertySource
  static void ketoProperties(DynamicPropertyRegistry registry) {
    var readAddress = "%s:%d".formatted(
        KETO_COMPOSE_CONTAINER.getServiceHost("keto-1",
            KETO_READ_PORT), KETO_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_READ_PORT));
    var writeAddress = "%s:%d".formatted(
        KETO_COMPOSE_CONTAINER.getServiceHost("keto-1",
            KETO_WRITE_PORT), KETO_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_WRITE_PORT));
    var oplAddress = "%s:%d".formatted(
        KETO_COMPOSE_CONTAINER.getServiceHost("keto-1",
            KETO_OPL_PORT), KETO_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_OPL_PORT));
    registry.add("spring.grpc.client.channels.keto-read.address", () -> readAddress);
    registry.add("spring.grpc.client.channels.keto-write.address", () -> writeAddress);
    registry.add("spring.grpc.client.channels.keto-opl.address", () -> oplAddress);
  }

  @BeforeEach
  void confirmRunningServiceAndCreateRelationship() throws IOException {
    assertThat(KETO_COMPOSE_CONTAINER.getContainerByServiceName("keto-1")).isPresent()
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
    var permissionResource = new ClassPathResource("authz/permissions.keto.json");
    Builder relationTuplesBuilder = TransactRelationTuplesRequest.newBuilder();
    parser.merge(new InputStreamReader(permissionResource.getInputStream()), relationTuplesBuilder);
  }

  @Test
  void testListNamespaces() {
    var namespaces = ketoAuthorizationService.listNamespaces();
    assertThat(namespaces).isNotNull()
        .isNotEmpty()
        .containsOnly("Actor", "Group", "Product", "ProductVariant", "Order", "Category");
  }

  @Test
  void testCheckSyntax() throws IOException {
    var parseResult = ketoAuthorizationService.checkSyntax(new PathResource("../keto/permissions.keto.ts"));
    assertThat(parseResult).isNotNull()
        .isEmpty();
  }

  @Nested
  class CheckPermissions {
    private static final String ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY = "neo";
    private static final String ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP = "trinity";

    @Test
    @DisplayName("Actor has read permission on ancestor category")
    void testHasReadPermissionAncestorCategory() {
      var categoryMedia = "daba3cfa-5a44-44a9-827e-b100094d06a6"; // Media
      var hasPermission = ketoAuthorizationService.checkPermission("Category", categoryMedia, "view", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Actor has no read permission on ancestor category")
    void testHasNoReadPermissionCategory() {
      var categoryMedia = "daba3cfa-5a44-44a9-827e-b100094d06a6"; // Media
      var hasPermission = ketoAuthorizationService.checkPermission("Category", categoryMedia, "view", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Actor has read permission on parent category")
    void testHasReadPermissionParentCategory() {
      var categoryVideos = "393ce9a2-c562-4dc1-8d67-2d28ab6e2776"; // Media > Videos
      var hasPermission = ketoAuthorizationService.checkPermission("Category", categoryVideos, "view", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission("Category", categoryVideos, "view", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermissionViaGroup).isTrue();
    }

    @Test
    @DisplayName("Actor has read permission on child category")
    void testHasReadPermissionChildCategory() {
      var categoryDocumentaries = "64b66eab-e1d0-4b00-ac64-a8cf1daebcc3"; // Media > Videos > Digital Downloads
      var hasPermission = ketoAuthorizationService.checkPermission("Category", categoryDocumentaries, "view", ACTOR_HAS_READ_ON_ANCESTOR_CATEGORY);
      assertThat(hasPermission).isTrue();
      var hasPermissionViaGroup = ketoAuthorizationService.checkPermission("Category", categoryDocumentaries, "view", ACTOR_HAS_READ_ON_PARENT_CATEGORY_VIA_GROUP_MEMBERSHIP);
      assertThat(hasPermissionViaGroup).isTrue();
    }
  }
}
