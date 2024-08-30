package com.simplecommerce.shared.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(mode = AdviceMode.ASPECTJ)
class SecurityConfiguration {

  /*
   * This filter enables OpenID Connect resource-server authentication for the application.
   */
  @Bean
  @Profile("oidc-auth")
  SecurityFilterChain oidcAuthFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests((requests) -> requests.requestMatchers(
        PathRequest.toStaticResources().atCommonLocations()).permitAll()
        .requestMatchers("/graphiql").permitAll()
        .anyRequest().authenticated());
    http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()));
    return http.build();
  }

  /*
   * This filter enables basic authentication for the application.
   */
  @Bean
  @Profile("!oidc-auth")
  SecurityFilterChain basicAuthFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
    http.csrf(AbstractHttpConfigurer::disable);
    http.formLogin(withDefaults());
    http.httpBasic(withDefaults());
    return http.build();
  }
}
