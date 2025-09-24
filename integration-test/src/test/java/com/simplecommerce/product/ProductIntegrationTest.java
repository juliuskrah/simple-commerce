package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.BaseDockerComposeTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ProductIntegrationTest extends BaseDockerComposeTest {

  private static final Logger LOG = LoggerFactory.getLogger(ProductIntegrationTest.class);

  HttpGraphQlTester http;

  @Override
  protected void assertSpecificService() {
    LOG.info("Verifying 'simple-commerce' service is running");
    assertThat(SIMPLE_COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName(SIMPLE_COMMERCE_SERVICE_NAME)).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
    assertThat(SIMPLE_COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName(DEX_SERVICE_NAME)).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
  }

  @BeforeEach
  void setupHttpGraphQlTester() {
    String hostname = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServiceHost(SIMPLE_COMMERCE_SERVICE_NAME, SIMPLE_COMMERCE_GRAPHQL_PORT);
    int port = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServicePort(SIMPLE_COMMERCE_SERVICE_NAME, SIMPLE_COMMERCE_GRAPHQL_PORT);
    String baseUrl = UriComponentsBuilder.newInstance().path("graphql").host(hostname).port(port).scheme("http").toUriString();
    LOG.info("Base URL for simple-commerce service: {}", baseUrl);
    http = HttpGraphQlTester.builder(
            WebTestClient.bindToServer().baseUrl(baseUrl)).build();
  }

  @Test
  void testAddProduct() {
    if (accessToken == null) {
      accessToken = getAccessToken();
    }
    LOG.info("Access Token: {}", accessToken);
    var tester = http.mutate().headers(headers -> headers.setBearerAuth(accessToken)).build();
    tester.documentName("product").operationName("createProduct")
        .variables(Map.of("input", Map.of("title", "House of Cards", "tags", List.of("drama", "politics"))))
        .execute()
        .path("addProduct").entity(new ParameterizedTypeReference<Map<String, Object>>() {})
        .satisfies(response -> assertThat(response).isNotEmpty()
            .containsEntry("slug", "house-of-cards"));
  }
}
