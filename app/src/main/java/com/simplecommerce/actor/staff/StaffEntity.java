package com.simplecommerce.actor.staff;

import com.simplecommerce.actor.ActorEntity;
import com.simplecommerce.actor.ActorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Staff entity representing admin users who can manage the system.
 * Staff members have access to the admin dashboard, product management, 
 * order processing, and other administrative functions.
 *
 * @author julius.krah
 */
@Entity(name = "Staff")
@Table(name = "staff")
public class StaffEntity extends ActorEntity {
  
  @Column
  private String department;
  
  @Column
  private String role;

  public StaffEntity() {
    super(ActorType.STAFF);
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "StaffEntity{" +
        "id=" + getId() +
        ", username='" + getUsername() + '\'' +
        ", email='" + getEmail() + '\'' +
        ", department='" + department + '\'' +
        ", role='" + role + '\'' +
        ", actorType=" + getActorType() +
        ", externalId='" + getExternalId() + '\'' +
        ", lastLogin=" + getLastLogin() +
        ", createdAt=" + getCreatedAt() +
        ", updatedAt=" + getUpdatedAt() +
        '}';
  }
}