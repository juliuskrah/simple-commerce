package com.simplecommerce.actor.customer;

import com.simplecommerce.actor.ActorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Customer entity representing users who interact with the storefront.
 * Customers can browse products, manage carts, and complete checkout flows.
 *
 * @author julius.krah
 */
@Entity(name = "Customer")
@Table(name = "customers")
public class CustomerEntity extends ActorEntity {

  @Column
  private String firstName;

  @Column
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CustomerGroup customerGroup = CustomerGroup.B2C;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public CustomerGroup getCustomerGroup() {
    return customerGroup;
  }

  public void setCustomerGroup(CustomerGroup customerGroup) {
    this.customerGroup = customerGroup;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof CustomerEntity that)) {
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
    return "CustomerEntity{" +
        "id=" + getId() +
        ", username='" + getUsername() + '\'' +
        ", email='" + getEmail() + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", customerGroup=" + customerGroup +
        ", externalId='" + getExternalId() + '\'' +
        ", lastLogin=" + getLastLogin() +
        ", createdAt=" + getCreatedDate() +
        ", updatedAt=" + getLastModifiedDate() +
        '}';
  }
}