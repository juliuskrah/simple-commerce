package com.simplecommerce.actor.user;

import com.simplecommerce.actor.ActorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Staff entity representing admin users who can manage the system.
 * Staff members have access to the admin dashboard, product management, 
 * order processing, and other administrative functions.
 *
 * @author julius.krah
 */
@Entity(name = "User")
@Table(name = "users")
public class UserEntity extends ActorEntity {
  
  @Column
  private String department;

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof UserEntity that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(getUsername());
    result = 31 * result + Objects.hashCode(getId());
    return result;
  }

  @Override
  public String toString() {
    return "UserEntity{" +
        "id=" + getId() +
        ", username='" + getUsername() + '\'' +
        ", email='" + getEmail() + '\'' +
        ", department='" + department + '\'' +
        ", externalId='" + getExternalId() + '\'' +
        ", lastLogin=" + getLastLogin() +
        ", createdAt=" + getCreatedDate() +
        ", updatedAt=" + getLastModifiedDate() +
        '}';
  }
}