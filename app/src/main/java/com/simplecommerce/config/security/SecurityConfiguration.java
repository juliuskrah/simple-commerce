package com.simplecommerce.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import com.simplecommerce.security.aspects.CheckAspect;
import com.simplecommerce.security.aspects.PermitAspect;
import jakarta.servlet.DispatcherType;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author julius.krah
 */
@Profile("oidc-authn")
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
  SecurityFilterChain oidcAuthFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(requests -> requests
            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint().excluding(ShutdownEndpoint.class)).permitAll()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers(GRAPHQL_PATH_PATTERNS).permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/graphql").permitAll()
            .requestMatchers(HttpMethod.POST, "/graphql").permitAll()
            .anyRequest().authenticated());
    http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));
    return http.build();
  }

  @Bean
  AuditEventRepository auditEventRepository() {
    return new InMemoryAuditEventRepository();
  }

  @Bean
  AuthorizationEventPublisher authorizationEventPublisher
      (ApplicationEventPublisher applicationEventPublisher) {
    return new SpringAuthorizationEventPublisher(applicationEventPublisher);
  }

  @Bean(name = "permitAspect$0")
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  PermitAspect permitAspect(MethodInterceptor preAuthorizeAuthorizationMethodInterceptor) {
    var aspect = PermitAspect.aspectOf();
    aspect.setSecurityInterceptor(preAuthorizeAuthorizationMethodInterceptor);
    return aspect;
  }

  @Bean(name = "checkAspect$0")
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  CheckAspect checkAspect(MethodInterceptor postAuthorizeAuthorizationMethodInterceptor) {
    var aspect = CheckAspect.aspectOf();
    aspect.setSecurityInterceptor(postAuthorizeAuthorizationMethodInterceptor);
    return aspect;
  }

  @Bean
  static AnnotationTemplateExpressionDefaults templateExpressionDefaults() {
    return new AnnotationTemplateExpressionDefaults();
  }
}
