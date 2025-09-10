package com.simplecommerce.actor.bot;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing bot entities.
 * Handles CRUD operations and business logic for bots.
 *
 * @author julius.krah
 */
@Service
@Transactional
public class BotService {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<BotEntity> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(BotEntity.class, id));
  }

  public Optional<BotEntity> findByUsername(String username) {
    try {
      BotEntity bot = entityManager
          .createQuery("FROM Bot WHERE username = :username", BotEntity.class)
          .setParameter("username", username)
          .getSingleResult();
      return Optional.of(bot);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<BotEntity> findByApiKey(String apiKey) {
    try {
      BotEntity bot = entityManager
          .createQuery("FROM Bot WHERE apiKey = :apiKey", BotEntity.class)
          .setParameter("apiKey", apiKey)
          .getSingleResult();
      return Optional.of(bot);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public List<BotEntity> findAll() {
    return entityManager
        .createQuery("FROM Bot ORDER BY username", BotEntity.class)
        .getResultList();
  }

  public List<BotEntity> findByAppId(String appId) {
    return entityManager
        .createQuery("FROM Bot WHERE appId = :appId ORDER BY username", BotEntity.class)
        .setParameter("appId", appId)
        .getResultList();
  }

  public BotEntity save(BotEntity bot) {
    if (bot.getId() == null) {
      entityManager.persist(bot);
      return bot;
    } else {
      return entityManager.merge(bot);
    }
  }

  public void deleteById(UUID id) {
    findById(id).ifPresent(entityManager::remove);
  }
}