package com.simplecommerce.actor.staff;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing staff entities.
 * Handles CRUD operations and business logic for staff members.
 *
 * @author julius.krah
 */
@Service
@Transactional
public class StaffService {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<StaffEntity> findById(UUID id) {
    return Optional.ofNullable(entityManager.find(StaffEntity.class, id));
  }

  public Optional<StaffEntity> findByUsername(String username) {
    try {
      StaffEntity staff = entityManager
          .createQuery("FROM Staff WHERE username = :username", StaffEntity.class)
          .setParameter("username", username)
          .getSingleResult();
      return Optional.of(staff);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public List<StaffEntity> findAll() {
    return entityManager
        .createQuery("FROM Staff ORDER BY username", StaffEntity.class)
        .getResultList();
  }

  public List<StaffEntity> findByDepartment(String department) {
    return entityManager
        .createQuery("FROM Staff WHERE department = :department ORDER BY username", StaffEntity.class)
        .setParameter("department", department)
        .getResultList();
  }

  public StaffEntity save(StaffEntity staff) {
    if (staff.getId() == null) {
      entityManager.persist(staff);
      return staff;
    } else {
      return entityManager.merge(staff);
    }
  }

  public void deleteById(UUID id) {
    findById(id).ifPresent(entityManager::remove);
  }
}