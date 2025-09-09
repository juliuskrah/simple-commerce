package com.simplecommerce.shared.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.simplecommerce.shared.authentication.DexAuthenticationService;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Collection;
import java.util.List;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(mode = AdviceMode.ASPECTJ)
@ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "servlet")
class SecurityConfiguration {
  
  private final Optional<DexAuthenticationService> dexAuthenticationService;
  
  public SecurityConfiguration(Optional<DexAuthenticationService> dexAuthenticationService) {
    this.dexAuthenticationService = dexAuthenticationService;
  }
  private static final String[] GRAPHQL_PATH_PATTERNS = {
      "/graphiql",
      "/graphql/schema"
  };

  /*
   * This filter enables OpenID Connect resource-server authentication for the application.
   */
  @Bean
  @Profile("oidc-authn")
  SecurityFilterChain oidcAuthFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(requests -> requests
            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint().excluding(ShutdownEndpoint.class)).permitAll()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers(GRAPHQL_PATH_PATTERNS).permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/graphql").permitAll()
            .anyRequest().authenticated());
    http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));
    return http.build();
  }

  /*
   * JWT Authentication Converter that extracts Actor information from tokens
   * and maps them to Spring Security authorities.
   */
  //  @Bean TODO: Disable until I decide what to do with it
  @Profile("oidc-authn")
  JwtAuthenticationConverter actorJwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractActorAuthorities);
    return converter;
  }

  /*
   * Extract Actor-based authorities from JWT token using DexAuthenticationService.
   */
  private Collection<GrantedAuthority> extractActorAuthorities(Jwt jwt) {
    try {
      String email = jwt.getClaimAsString("email");
      String username = jwt.getClaimAsString("preferred_username");
      
      // Use DexAuthenticationService to determine actor type
      String actorType = determineActorType(email, username);
      
      return List.of(
          new SimpleGrantedAuthority("ROLE_" + actorType.toUpperCase()),
          new SimpleGrantedAuthority("ACTOR_" + actorType.toUpperCase())
      );
    } catch (Exception e) {
      return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
  }

  /*
   * Determine actor type using the same logic as DexAuthenticationService.
   */
  private String determineActorType(String email, String username) {
    if (email != null) {
      if (email.contains("customer")) {
        return "Customer";
      } else if (email.contains("vendor") || email.contains("staff")) {
        return "Staff";
      } else if (email.contains("bot") || email.contains("service")) {
        return "Bot";
      }
    }
    
    if (username != null) {
      if (username.contains("admin") || username.equals("simple_commerce")) {
        return "Staff";
      } else if (username.contains("customer")) {
        return "Customer";
      } else if (username.contains("bot")) {
        return "Bot";
      }
    }
    
    // Default to Customer for unknown users
    return "Customer";
  }

  /*
   * This filter enables basic authentication for the application.
   */
  @Bean
  @Profile("!oidc-authn")
  SecurityFilterChain basicAuthFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(requests -> requests.requestMatchers(
        PathRequest.toStaticResources().atCommonLocations()).permitAll()
        .requestMatchers(GRAPHQL_PATH_PATTERNS).permitAll()
        .anyRequest().authenticated());
    http.csrf(AbstractHttpConfigurer::disable);
    http.formLogin(withDefaults());
    http.httpBasic(withDefaults());
    return http.build();
  }
}
