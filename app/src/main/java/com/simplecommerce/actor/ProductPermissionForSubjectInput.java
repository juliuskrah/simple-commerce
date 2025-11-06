package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.ResourcePermittedInput;
import java.util.List;

/// @author julius.krah
public record ProductPermissionForSubjectInput(ResourcePermittedInput subject, List<String> productIds, BasePermissions permission) {

}
