package com.simplecommerce.order;

import org.jspecify.annotations.Nullable;

/**
 * Represents a shipping or billing address.
 *
 * @author julius.krah
 */
public record Address(
    @Nullable String firstName,
    @Nullable String lastName,
    @Nullable String company,
    @Nullable String addressLine1,
    @Nullable String addressLine2,
    @Nullable String city,
    @Nullable String state,
    @Nullable String postalCode,
    @Nullable String country,
    @Nullable String phone
) {
  /**
   * Returns the full name (firstName + lastName).
   *
   * @return the full name
   */
  public String fullName() {
    if (firstName == null && lastName == null) {
      return "";
    }
    if (firstName == null) {
      return lastName;
    }
    if (lastName == null) {
      return firstName;
    }
    return firstName + " " + lastName;
  }
}
