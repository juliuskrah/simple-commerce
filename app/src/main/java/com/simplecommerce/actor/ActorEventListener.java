package com.simplecommerce.actor;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;

import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.shared.types.UserType;
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
import org.springframework.stereotype.Component;

/**
 * Event listener that handles authentication events and creates/updates actors
 * in the system. This implements JIT (Just In Time) provisioning from dexidp.
 *
 * @author julius.krah
 */
@Async
@Component
public class ActorEventListener {
  @PersistenceUnit
  private EntityManagerFactory emf;
  private static final Logger LOG = LoggerFactory.getLogger(ActorEventListener.class);


  private UserEntity mapToUserEntity(JwtAuthenticationToken jwt) {
    UserEntity entity = new UserEntity();
    entity.setUserType(UserType.STAFF);
    mapCommonFields(entity, jwt);

    return entity;
  }

  private void mapCommonFields(ActorEntity entity, JwtAuthenticationToken jwt) {
    entity.setUsername(jwt.getName());
    entity.setEmail(jwt.getToken().getClaimAsString("email"));
    entity.setExternalId(jwt.getToken().getSubject());
    if (Objects.nonNull(jwt.getToken().getIssuedAt()) && entity instanceof UserEntity userEntity) {
      userEntity.setLastLogin(requireNonNull(jwt.getToken().getIssuedAt()).atOffset(UTC));
    }
  }

  /**
   * Upserts an actor in the database. Creates new actor if not exists,
   * otherwise updates the last login time.
   */
  private void upsertActor(UserEntity entity) {
    var lastLogin = entity.getLastLogin();
    var sessionFactory = emf.unwrap(SessionFactory.class);
    
    sessionFactory.inTransaction(session -> {
      var query = session.createQuery("FROM User WHERE username = :username", UserEntity.class);
      var existingActor = query.setParameter("username", entity.getUsername())
          .uniqueResultOptional();
      
      if (existingActor.isPresent()) {
        // Update existing actor
        existingActor.get().setLastLogin(lastLogin);
      } else {
        // Create new actor
        session.persist(entity);
      }
    });
  }

  @EventListener
  void on(AuthenticationSuccessEvent event) {
    if (event.getAuthentication() instanceof JwtAuthenticationToken jwt) {
      LOG.debug("Authentication success for: {}", jwt.getName());

      UserEntity actor = mapToUserEntity(jwt);
      upsertActor(actor);
      
      LOG.debug("{} logged in at {}", actor.getUsername(), actor.getLastLogin());
    }
  }

  @EventListener
  void on(AuthenticationFailureBadCredentialsEvent event) {
    LOG.debug("Authentication failure: {}", event.getAuthentication());
  }

  @EventListener
  void on(AuthorizationDeniedEvent<?> event) {
    LOG.debug("Authorization denied: {}", event.getSource());
  }
}