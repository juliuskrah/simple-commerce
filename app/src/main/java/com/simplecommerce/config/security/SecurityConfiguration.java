package com.simplecommerce.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(mode = AdviceMode.ASPECTJ)
@ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "servlet")
class SecurityConfiguration {

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
}
