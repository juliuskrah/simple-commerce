package com.simplecommerce.customer;

import com.simplecommerce.actor.ActorEntity;
import com.simplecommerce.actor.ActorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Customer entity representing users who interact with the storefront.
 * Customers can browse products, manage carts, and complete checkout flows.
 *
 * @author julius.krah
 */
@Entity(name = "Customer")
@Table(name = "customer")
public class CustomerEntity extends ActorEntity {

  @Column
  private String firstName;

  @Column
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CustomerGroup customerGroup = CustomerGroup.B2C;

  public CustomerEntity() {
    super(ActorType.CUSTOMER);
  }

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
  public String toString() {
    return "CustomerEntity{" +
        "id=" + getId() +
        ", username='" + getUsername() + '\'' +
        ", email='" + getEmail() + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", customerGroup=" + customerGroup +
        ", actorType=" + getActorType() +
        ", externalId='" + getExternalId() + '\'' +
        ", lastLogin=" + getLastLogin() +
        ", createdAt=" + getCreatedAt() +
        ", updatedAt=" + getUpdatedAt() +
        '}';
  }
}