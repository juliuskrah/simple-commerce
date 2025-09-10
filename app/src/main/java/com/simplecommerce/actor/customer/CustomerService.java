package com.simplecommerce.actor.customer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing customer entities.
 * Handles CRUD operations and business logic for customers.
 *
 * @author julius.krah
 */
@Service
@Transactional
public class CustomerService {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<CustomerEntity> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(CustomerEntity.class, id));
  }

  public Optional<CustomerEntity> findByUsername(String username) {
    try {
      CustomerEntity customer = entityManager
          .createQuery("FROM Customer WHERE username = :username", CustomerEntity.class)
          .setParameter("username", username)
          .getSingleResult();
      return Optional.of(customer);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public List<CustomerEntity> findAll() {
    return entityManager
        .createQuery("FROM Customer ORDER BY username", CustomerEntity.class)
        .getResultList();
  }

  public List<CustomerEntity> findByCustomerGroup(CustomerGroup customerGroup) {
    return entityManager
        .createQuery("FROM Customer WHERE customerGroup = :customerGroup ORDER BY username", CustomerEntity.class)
        .setParameter("customerGroup", customerGroup)
        .getResultList();
  }

  public CustomerEntity save(CustomerEntity customer) {
    if (customer.getId() == null) {
      entityManager.persist(customer);
      return customer;
    } else {
      return entityManager.merge(customer);
    }
  }

  public void deleteById(UUID id) {
    findById(id).ifPresent(entityManager::remove);
  }
}