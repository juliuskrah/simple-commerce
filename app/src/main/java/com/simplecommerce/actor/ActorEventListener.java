package com.simplecommerce.actor;

import static java.time.ZoneOffset.UTC;

import com.simplecommerce.bot.BotEntity;
import com.simplecommerce.customer.CustomerEntity;
import com.simplecommerce.customer.CustomerGroup;
import com.simplecommerce.staff.StaffEntity;
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

  /**
   * Determines the actor type based on JWT claims and user attributes.
   * This can be enhanced to use custom claims from dexidp.
   */
  private ActorType determineActorType(JwtAuthenticationToken jwt) {
    String email = jwt.getToken().getClaimAsString("email");
    String username = jwt.getName();
    
    // Logic for determining actor type:
    // 1. Check for custom actor_type claim from dexidp
    String actorTypeClaim = jwt.getToken().getClaimAsString("actor_type");
    if (actorTypeClaim != null) {
      try {
        return ActorType.valueOf(actorTypeClaim.toUpperCase());
      } catch (IllegalArgumentException e) {
        LOG.warn("Invalid actor_type claim: {}", actorTypeClaim);
      }
    }
    
    // 2. Email domain-based detection
    if (email != null) {
      // Admin/company email domains -> STAFF
      if (email.endsWith("@simple-commerce.com") || email.endsWith("@example.com")) {
        return ActorType.STAFF;
      }
    }
    
    // 3. Bot detection (username pattern or specific claim)
    if (username != null && (username.toLowerCase().contains("bot") || username.toLowerCase().contains("api"))) {
      return ActorType.BOT;
    }
    
    // 4. Default to CUSTOMER for storefront users
    return ActorType.CUSTOMER;
  }

  /**
   * Creates an actor entity based on the determined type.
   */
  private ActorEntity createActorByType(JwtAuthenticationToken jwt, ActorType actorType) {
    return switch (actorType) {
      case STAFF -> mapToStaffEntity(jwt);
      case CUSTOMER -> mapToCustomerEntity(jwt);
      case BOT -> mapToBotEntity(jwt);
    };
  }

  private StaffEntity mapToStaffEntity(JwtAuthenticationToken jwt) {
    StaffEntity entity = new StaffEntity();
    mapCommonFields(entity, jwt);
    
    // Map staff-specific fields from JWT claims
    entity.setDepartment(jwt.getToken().getClaimAsString("department"));
    entity.setRole(jwt.getToken().getClaimAsString("role"));
    
    return entity;
  }

  private CustomerEntity mapToCustomerEntity(JwtAuthenticationToken jwt) {
    CustomerEntity entity = new CustomerEntity();
    mapCommonFields(entity, jwt);
    
    // Map customer-specific fields from JWT claims
    entity.setFirstName(jwt.getToken().getClaimAsString("given_name"));
    entity.setLastName(jwt.getToken().getClaimAsString("family_name"));
    
    // Determine customer group from claims or email domain
    String customerGroupClaim = jwt.getToken().getClaimAsString("customer_group");
    if (customerGroupClaim != null) {
      try {
        entity.setCustomerGroup(CustomerGroup.valueOf(customerGroupClaim.toUpperCase()));
      } catch (IllegalArgumentException e) {
        LOG.warn("Invalid customer_group claim: {}", customerGroupClaim);
        entity.setCustomerGroup(CustomerGroup.B2C);
      }
    } else {
      entity.setCustomerGroup(CustomerGroup.B2C);
    }
    
    return entity;
  }

  private BotEntity mapToBotEntity(JwtAuthenticationToken jwt) {
    BotEntity entity = new BotEntity();
    mapCommonFields(entity, jwt);
    
    // Map bot-specific fields from JWT claims
    entity.setAppId(jwt.getToken().getClaimAsString("app_id"));
    entity.setPermissions(jwt.getToken().getClaimAsString("permissions")); // JSON string
    
    return entity;
  }

  private void mapCommonFields(ActorEntity entity, JwtAuthenticationToken jwt) {
    entity.setUsername(jwt.getName());
    entity.setEmail(jwt.getToken().getClaimAsString("email"));
    entity.setExternalId(jwt.getToken().getSubject());
    if (Objects.nonNull(jwt.getToken().getIssuedAt())) {
      entity.setLastLogin(jwt.getToken().getIssuedAt().atOffset(UTC));
    }
  }

  /**
   * Upserts an actor in the database. Creates new actor if not exists,
   * otherwise updates the last login time.
   */
  private ActorEntity upsertActor(ActorEntity entity) {
    var lastLogin = entity.getLastLogin();
    var sessionFactory = emf.unwrap(SessionFactory.class);
    var actorType = entity.getActorType();
    
    sessionFactory.inTransaction(session -> {
      // Query the appropriate table based on actor type
      String entityName = switch (actorType) {
        case STAFF -> "Staff";
        case CUSTOMER -> "Customer";
        case BOT -> "Bot";
      };
      
      Class<?> entityClass = switch (actorType) {
        case STAFF -> StaffEntity.class;
        case CUSTOMER -> CustomerEntity.class;
        case BOT -> BotEntity.class;
      };
      
      @SuppressWarnings("unchecked")
      var query = session.createQuery("FROM " + entityName + " WHERE username = :username", (Class<ActorEntity>) entityClass);
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
    
    return entity;
  }

  @EventListener
  void on(AuthenticationSuccessEvent event) {
    if (event.getAuthentication() instanceof JwtAuthenticationToken jwt) {
      LOG.debug("Authentication success for: {}", jwt.getName());
      
      ActorType actorType = determineActorType(jwt);
      ActorEntity actor = createActorByType(jwt, actorType);
      upsertActor(actor);
      
      LOG.debug("{} actor logged in: {}", actorType, actor.getUsername());
    }
  }

  @EventListener
  void on(AuthenticationFailureBadCredentialsEvent event) {
    LOG.debug("Authentication failure: {}", event.getAuthentication());
  }

  @EventListener
  void on(AuthorizationDeniedEvent<SecurityContextHolderAwareRequestWrapper> event) {
    LOG.debug("Authorization denied: {}", event.getSource());
  }
}