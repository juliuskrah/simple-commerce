package com.simplecommerce.shared.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.config.security.KetoConfiguration;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.PathResource;
import org.springframework.grpc.autoconfigure.client.GrpcClientAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles({"test", "keto-authz"})
@Import({KetoConfiguration.class, GrpcClientAutoConfiguration.class, SslAutoConfiguration.class})
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = KetoAuthorizationService.class)
class KetoAuthorizationServiceTest {

  static final int KETO_READ_PORT = 4466;
  static final int KETO_OPL_PORT = 4469;
  @Container
  static final ComposeContainer KETO_COMPOSE_CONTAINER = new ComposeContainer(
      new File("../compose.yaml")
  )
      .withOptions("--profile", "keto-authz")
      .withExposedService("keto", 1, KETO_READ_PORT)
      .withExposedService("keto", 1, KETO_OPL_PORT);

  @Autowired
  KetoAuthorizationService ketoAuthorizationService;

  @DynamicPropertySource
  static void ketoProperties(DynamicPropertyRegistry registry) {
    var readAddress = "%s:%d".formatted(
        KETO_COMPOSE_CONTAINER.getServiceHost("keto-1",
            KETO_READ_PORT), KETO_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_READ_PORT));
    var oplAddress = "%s:%d".formatted(
        KETO_COMPOSE_CONTAINER.getServiceHost("keto-1",
            KETO_OPL_PORT), KETO_COMPOSE_CONTAINER.getServicePort("keto-1", KETO_OPL_PORT));
    registry.add("spring.grpc.client.channels.keto-read.address", () -> readAddress);
    registry.add("spring.grpc.client.channels.keto-opl.address", () -> oplAddress);
  }

  @BeforeEach
  void confirmRunningService() {
    assertThat(KETO_COMPOSE_CONTAINER.getContainerByServiceName("keto-1")).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
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
}
