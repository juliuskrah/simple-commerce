package com.simplecommerce.shared.utils;

import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

  private static final String USERNAME_CLAIM = "preferred_username";

  private SecurityUtils() {
  }

  /**
   * Get the login of the current user.
   *
   * @return the login of the current user.
   */
  public static Optional<String> getCurrentUserLogin() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
  }

  private static String extractPrincipal(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
      return springSecurityUser.getUsername();
    } else if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
      return jwtAuthenticationToken.getName();
    } else if (authentication.getPrincipal() instanceof DefaultOidcUser defaultOidcUser) {
      Map<String, Object> attributes = defaultOidcUser.getAttributes();
      if (attributes.containsKey(USERNAME_CLAIM)) {
        return (String) attributes.get(USERNAME_CLAIM);
      }
    } else if (authentication.getPrincipal() instanceof String s) {
      return s;
    }
    return null;
  }

  /**
   * Check if a user is authenticated.
   *
   * @return true if the user is authenticated, false otherwise.
   */
  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated();
  }
}

