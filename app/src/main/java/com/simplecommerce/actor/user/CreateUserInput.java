package com.simplecommerce.actor.user;

import com.simplecommerce.shared.types.UserType;
import jakarta.validation.constraints.Email;

/// @author julius.krah
public record CreateUserInput(
    String username,
    @Email
    String email,
    UserType userType
) {

}
