package com.simplecommerce.user;

import static java.time.ZoneOffset.UTC;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.util.Objects;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Async
@Component
public class UserEventListener {
  @PersistenceUnit
  private EntityManagerFactory emf;
  private static final Logger LOG = LoggerFactory.getLogger(UserEventListener.class);

  private UserEntity mapToEntity(JwtAuthenticationToken jwt) {
    UserEntity entity = new UserEntity();
    entity.setUsername(jwt.getName());
    entity.setEmail(jwt.getToken().getClaimAsString("email"));
    entity.setExternalId(jwt.getToken().getSubject());
    if(Objects.nonNull(jwt.getToken().getIssuedAt())) {
      entity.setLastLogin(jwt.getToken().getIssuedAt().atOffset(UTC));
    }
    return entity;
  }

  private UserEntity upsertUser(UserEntity entity) {
    var lastLogin = entity.getLastLogin();
    var sessionFactory = emf.unwrap(SessionFactory.class);
    sessionFactory.inTransaction(session ->
      session.createQuery("FROM User WHERE username = :username", UserEntity.class)
          .setParameter("username", entity.getUsername())
          .uniqueResultOptional()
          .orElseGet(() -> {
            session.persist(entity);
            return entity;
          }).setLastLogin(lastLogin) // Update last login
    );
    return entity;
  }

  @EventListener
  void on(AuthenticationSuccessEvent event) {
    if (event.getAuthentication() instanceof JwtAuthenticationToken jwt) {
      LOG.debug("Authentication success: {}", jwt);
      var user = upsertUser(mapToEntity(jwt));
      LOG.debug("User logged in: {}", user);
    }
  }

  @EventListener
  void on(AuthenticationFailureBadCredentialsEvent event) {
    LOG.debug("Authentication failure: {}", event.getAuthentication());
  }

  @EventListener
  void on(AuthorizationDeniedEvent<SecurityContextHolderAwareRequestWrapper> event) {
    LOG.debug("Catch all authorization failure: {}", event.getSource());
  }
}
