package com.simplecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;

/// A base test class for integration tests using Docker Compose.
///
/// @author julius.krah
public abstract class BaseDockerComposeTest {

  private static final String CLIENT_ID = "simple-commerce";
  private static final String CLIENT_SECRET = "Zm9vYmFy";
  private static final int DEX_IDP_PORT = 5556;

  protected static final String DEX_SERVICE_NAME = "oidc-1";
  protected static final int SIMPLE_COMMERCE_GRAPHQL_PORT = 8080;
  protected static final String SIMPLE_COMMERCE_SERVICE_NAME = "simple-commerce-1";
  protected static final String ADMIN_USERNAME = "julius.krah@example.com";
  protected static final String ADMIN_PASSWORD = "simple_commerce";
  protected static String accessToken;

  @BeforeEach
  void assertDatabaseService() {
    assertThat(SIMPLE_COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName("postgres-1")).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
    assertSpecificService();
  }

  @Container
  protected static final ComposeContainer SIMPLE_COMMERCE_COMPOSE_CONTAINER = new ComposeContainer(
      new File("../compose.yaml"))
      .withEnv("COMPOSE_PROFILES", "oidc-authn,app,keto-authz")
      .withExposedService("simple-commerce", 1, SIMPLE_COMMERCE_GRAPHQL_PORT)
      .withExposedService("oidc", 1, DEX_IDP_PORT);

  protected String getAccessToken() {
    var hostname = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServiceHost(DEX_SERVICE_NAME, DEX_IDP_PORT);
    var port = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServicePort(DEX_SERVICE_NAME, DEX_IDP_PORT);
    var context = new AuthenticationContext(hostname, port, CLIENT_ID, CLIENT_SECRET, ADMIN_USERNAME, ADMIN_PASSWORD);
    return AuthenticationContextHolder.setContext(context)
        .call(AuthenticationUtils::getAccessToken);
  }

  protected abstract void assertSpecificService();
}
