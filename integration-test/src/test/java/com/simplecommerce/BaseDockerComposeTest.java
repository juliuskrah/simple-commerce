package com.simplecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class BaseDockerComposeTest {

  protected static final int SIMPLE_COMMERCE_GRAPHQL_PORT = 8080;
  protected static final int DEX_IDP_PORT = 5556;
  protected static final String SIMPLE_COMMERCE_SERVICE_NAME = "simple-commerce-1";
  protected static String accessToken;

  @Container
  protected static final ComposeContainer SIMPLE_COMMERCE_COMPOSE_CONTAINER = new ComposeContainer(
      new File("../compose.yaml"))
      .withEnv("COMPOSE_PROFILES", "oidc-authn,app,keto-authz")
      .withExposedService("simple-commerce", 1, SIMPLE_COMMERCE_GRAPHQL_PORT)
      .withExposedService("oidc", 1, DEX_IDP_PORT);

  @BeforeEach
  void assertDatabaseService() {
    assertThat(SIMPLE_COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName("postgres-1")).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
    assertSpecificService();
  }

  protected abstract void assertSpecificService();
}
