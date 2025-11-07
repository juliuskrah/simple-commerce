package com.simplecommerce;

import static java.lang.ScopedValue.where;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/// Utility class for handling authentication and obtaining access tokens using OAuth2 authorization code flow.
/// This class leverages Playwright for browser automation to simulate user login and consent.
/// It also uses Java's ScopedValue to manage client credentials securely within a specific scope.
/// @author julius.krah
public final class AuthenticationUtils {
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationUtils.class);
  private static final ScopedValue<ClientCredentials> CLIENT_CREDENTIALS_SCOPED_VALUE = ScopedValue.newInstance();

  record ClientCredentials(String clientId, String clientSecret) {}

  private AuthenticationUtils() {
  }

  private static String codeForToken(URI tokenUri, String code, String redirectUrl) {
    var parts = new LinkedMultiValueMap<>();
    var clientCredentials = CLIENT_CREDENTIALS_SCOPED_VALUE.get();
    parts.add("grant_type", "authorization_code");
    parts.add("client_id", clientCredentials.clientId);
    parts.add("client_secret", clientCredentials.clientSecret);
    parts.add("code", code);
    parts.add("redirect_uri", redirectUrl);
    RestClient restClient = RestClient.create(tokenUri);
    var token = restClient.post().contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(parts).retrieve()
        .body(new ParameterizedTypeReference<Map<String, String>>() {
        });
    return token.get("access_token");
  }

  public static String getAccessToken() {
    var authContext = AuthenticationContextHolder.getContext();
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
      URI baseUrl = UriComponentsBuilder.newInstance().scheme("http").host(authContext.hostname()).port(authContext.port()).build().toUri();
      String authUrl = UriComponentsBuilder.fromUri(baseUrl).path("/dex/auth")
          .queryParam("client_id", authContext.clientId())
          .queryParam("response_type", "code")
          .queryParam("scope", "openid email offline_access profile")
          .queryParam("redirect_uri", redirectUri)
          .queryParam("audience", "simple_commerce")
          .queryParam("state", state)
          .toUriString();
      page.navigate(authUrl);

      page.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("email address")).fill(authContext.username());
      page.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("Password")).fill(authContext.password());
      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Login")).click();

      if (page.url().contains("/approval")) {
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Grant Access")).click();
      }
      var tokenUrl = baseUrl.resolve("/dex/token");
      var authCode = authCodeFuture.join();
      return where(CLIENT_CREDENTIALS_SCOPED_VALUE, new ClientCredentials(authContext.clientId(), authContext.clientSecret()))
          .call(() -> codeForToken(tokenUrl, authCode, redirectUri));
    }
  }
}
