package com.simplecommerce.actor;

import com.simplecommerce.bot.BotEntity;
import com.simplecommerce.customer.CustomerEntity;
import com.simplecommerce.staff.StaffEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing actors across all types.
 * Provides common operations for Staff, Customer, and Bot entities.
 *
 * @author julius.krah
 */
@Service
@Transactional
public class ActorService {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Find an actor by ID, regardless of type.
   */
  public Optional<ActorEntity> findActorById(UUID id) {
    // Try each actor type
    Optional<StaffEntity> staff = Optional.ofNullable(entityManager.find(StaffEntity.class, id));
    if (staff.isPresent()) {
      return staff.map(s -> s);
    }

    Optional<CustomerEntity> customer = Optional.ofNullable(entityManager.find(CustomerEntity.class, id));
    if (customer.isPresent()) {
      return customer.map(c -> c);
    }

    Optional<BotEntity> bot = Optional.ofNullable(entityManager.find(BotEntity.class, id));
    return bot.map(b -> b);
  }

  /**
   * Find an actor by username, regardless of type.
   */
  public Optional<ActorEntity> findActorByUsername(String username) {
    // Try staff first
    try {
      StaffEntity staff = entityManager
          .createQuery("FROM Staff WHERE username = :username", StaffEntity.class)
          .setParameter("username", username)
          .getSingleResult();
      return Optional.of(staff);
    } catch (Exception e) {
      // Continue to next type
    }

    // Try customer
    try {
      CustomerEntity customer = entityManager
          .createQuery("FROM Customer WHERE username = :username", CustomerEntity.class)
          .setParameter("username", username)
          .getSingleResult();
      return Optional.of(customer);
    } catch (Exception e) {
      // Continue to next type
    }

    // Try bot
    try {
      BotEntity bot = entityManager
          .createQuery("FROM Bot WHERE username = :username", BotEntity.class)
          .setParameter("username", username)
          .getSingleResult();
      return Optional.of(bot);
    } catch (Exception e) {
      // Not found
    }

    return Optional.empty();
  }

  /**
   * Find an actor by external ID (from dexidp).
   */
  public Optional<ActorEntity> findActorByExternalId(String externalId) {
    // Try staff first
    try {
      StaffEntity staff = entityManager
          .createQuery("FROM Staff WHERE externalId = :externalId", StaffEntity.class)
          .setParameter("externalId", externalId)
          .getSingleResult();
      return Optional.of(staff);
    } catch (Exception e) {
      // Continue to next type
    }

    // Try customer
    try {
      CustomerEntity customer = entityManager
          .createQuery("FROM Customer WHERE externalId = :externalId", CustomerEntity.class)
          .setParameter("externalId", externalId)
          .getSingleResult();
      return Optional.of(customer);
    } catch (Exception e) {
      // Continue to next type
    }

    // Try bot
    try {
      BotEntity bot = entityManager
          .createQuery("FROM Bot WHERE externalId = :externalId", BotEntity.class)
          .setParameter("externalId", externalId)
          .getSingleResult();
      return Optional.of(bot);
    } catch (Exception e) {
      // Not found
    }

    return Optional.empty();
  }
}