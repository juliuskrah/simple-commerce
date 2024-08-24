package com.simplecommerce.shared.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

  @Bean
  SecurityFilterChain commerceSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests((requests) -> requests.requestMatchers(
        PathRequest.toStaticResources().atCommonLocations()).permitAll()
        .requestMatchers("/graphiql").permitAll()
        .anyRequest().authenticated());
    http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()));
    return http.build();
  }
}
