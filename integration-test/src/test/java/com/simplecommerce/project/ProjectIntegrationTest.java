package com.simplecommerce.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.simplecommerce.BaseDockerComposeTest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ProjectIntegrationTest extends BaseDockerComposeTest {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectIntegrationTest.class);
  private static final String DEX_SERVICE_NAME = "oidc-1";
  private static final String CLIENT_ID = "simple-commerce";
  private static final String CLIENT_SECRET = "Zm9vYmFy";

  HttpGraphQlTester http;

  private String token(URI uri, String code, String redirectUrl) {
    var parts = new LinkedMultiValueMap<>();
    parts.add("grant_type", "authorization_code");
    parts.add("client_id", CLIENT_ID);
    parts.add("client_secret", CLIENT_SECRET);
    parts.add("code", code);
    parts.add("redirect_uri", redirectUrl);
    RestClient restClient = RestClient.create(uri);
    var token = restClient.post().contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(parts).retrieve()
        .body(new ParameterizedTypeReference<Map<String, String>>() {
        });
    return token.get("access_token");
  }

  @Override
  protected void assertSpecificService() {
    LOG.info("Verifying 'simple-commerce' service is running");
    assertThat(SIMPLE_COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName(SIMPLE_COMMERCE_SERVICE_NAME)).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
    assertThat(SIMPLE_COMMERCE_COMPOSE_CONTAINER.getContainerByServiceName(DEX_SERVICE_NAME)).isPresent()
        .get().hasFieldOrPropertyWithValue("running", true);
  }

  @BeforeEach
  void setupHttpClient() {
    String hostname = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServiceHost(SIMPLE_COMMERCE_SERVICE_NAME, SIMPLE_COMMERCE_GRAPHQL_PORT);
    int port = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServicePort(SIMPLE_COMMERCE_SERVICE_NAME, SIMPLE_COMMERCE_GRAPHQL_PORT);
    String baseUrl = "http://" + hostname + ":" + port + "/graphql";
    LOG.info("Base URL for simple-commerce service: {}", baseUrl);
    http = HttpGraphQlTester.builder(
            WebTestClient.bindToServer().baseUrl(baseUrl)).build();
  }

  String getAccessToken() {
    String hostname = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServiceHost(DEX_SERVICE_NAME, DEX_IDP_PORT);
    int port = SIMPLE_COMMERCE_COMPOSE_CONTAINER.getServicePort(DEX_SERVICE_NAME, DEX_IDP_PORT);
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      CompletableFuture<String> authCodeFuture = new CompletableFuture<>();

      String state = UUID.randomUUID().toString();
      String redirectUri = "http://127.0.0.1:8080/callback";
      page.onRequest(request -> {
        if (request.url().startsWith(redirectUri) && request.url().contains("code=")) {
          LOG.info("Auth code URI: {}", request.url());
          var queryParams = UriComponentsBuilder.fromUriString(request.url()).build().getQueryParams();
          if (!state.equals(queryParams.getFirst("state"))) {
            throw new IllegalStateException("State does not match");
          }
          String code = queryParams.getFirst("code");
          authCodeFuture.complete(code);
        }
      });
      String baseUrl = "http://" + hostname + ":" + port;
      String authUrl = baseUrl
          + "/dex/auth?client_id=" + CLIENT_ID
          + "&response_type=code&scope=openid%20email%20offline_access&redirect_uri="
          + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
          + "&audience=simple_commerce&state=" + state;
      page.navigate(authUrl);

      page.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("email address")).fill("julius.krah@example.com");
      page.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("Password")).fill("simple_commerce");
      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Login")).click();

      if (page.url().contains("/approval")) {
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Grant Access")).click();
      }
      var tokenUrl = baseUrl + "/dex/token";
      var authCode = authCodeFuture.join();
      return token(URI.create(tokenUrl), authCode, redirectUri);
    }
  }

  @Test
  void testAddProject() {
    if (accessToken == null) {
      accessToken = getAccessToken();
    }
    LOG.info("Access Token: {}", accessToken);
    var tester = http.mutate().headers(headers -> headers.setBearerAuth(accessToken)).build();
    tester.documentName("product").operationName("createProduct")
        .variables(Map.of("input", Map.of("title", "House of Cards", "tags", List.of("drama", "politics"))))
        .execute()
        .path("addProduct").entity(new ParameterizedTypeReference<Map<String, Object>>() {})
        .satisfies(response -> {
          assertThat(response).isNotEmpty()
              .containsEntry("slug", "house-of-cards");
        });
  }
}
