package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.ResourcePermittedInput;
import com.simplecommerce.shared.types.RoleAssigneeInput;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing actors in the system. Provides methods to find, create, update, and delete actors.
 *
 * @author julius.krah
 * @since 1.0
 */
public interface ActorService {

  Optional<Actor> findActor(String username);

  RoleAssignable addRolesToSubject(List<String> roles, RoleAssigneeInput subject);

  RoleAssignable removeRolesFromSubject(List<String> roles, RoleAssigneeInput subject);

  ResourcePermissible assignProductPermission(ResourcePermittedInput subject, List<String> productIds, BasePermissions permission);

  ResourcePermissible revokeProductPermission(ResourcePermittedInput subject, List<String> productIds, BasePermissions permission);

  Optional<Role> findRoleByName(String roleName);

  List<Role> findRoles();

  List<BasePermissions> findPermissions();
}
