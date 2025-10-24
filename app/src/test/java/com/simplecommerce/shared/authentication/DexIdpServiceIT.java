package com.simplecommerce.shared.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.config.security.DexConfiguration;
import com.simplecommerce.shared.authentication.DexIdpServiceIT.DexInnerConfiguration;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.grpc.autoconfigure.client.GrpcClientAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/// @author julius.krah
@Testcontainers
@ActiveProfiles({"test", "oidc-authn"})
@Import({DexConfiguration.class, GrpcClientAutoConfiguration.class, SslAutoConfiguration.class, DexInnerConfiguration.class})
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = DexIdpService.class)
class DexIdpServiceIT {

  @Autowired
  private DexIdpService dexIdpService;
  private static final int DEX_GRPC_PORT = 5557;
  @Container
  static final ComposeContainer COMMERCE_COMPOSE_CONTAINER = new ComposeContainer(
      new File("../compose.yaml"))
      .withEnv("COMPOSE_PROFILES", "oidc-authn,keto-authz")
      .withExposedService("oidc", 1, DEX_GRPC_PORT);

  @TestConfiguration
  static class DexInnerConfiguration {

    @Bean
    DynamicPropertyRegistrar dexProperties() {
      var dexGrpcAddress = "%s:%d".formatted(
          COMMERCE_COMPOSE_CONTAINER.getServiceHost("oidc-1",
              DEX_GRPC_PORT), COMMERCE_COMPOSE_CONTAINER.getServicePort("oidc-1", DEX_GRPC_PORT));
      return registry -> registry.add("spring.grpc.client.channels.keto-read.address", () -> dexGrpcAddress);
    }
  }

  @BeforeAll
  static void confirmRunningService() {
    assertThat(COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName("oidc-1")).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
  }

  @Test
  void testCreateUser() {
    var alreadyExists = dexIdpService.addUser("user@example.org", "user", "user123");
    assertThat(alreadyExists).isFalse();
    var verified = dexIdpService.verifyLogin("user@example.org", "user123");
    assertThat(verified).isTrue();
  }

  @Test
  void testListUsers() {
    var users = dexIdpService.listUsers();
    assertThat(users).isNotEmpty().hasSize(4); // see dex/config.yaml
  }
}
