package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.Role;
import com.simplecommerce.shared.types.SubjectInput;
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

  PermissionAssignmentPayload addRolesToSubject(List<String> roles, SubjectInput subject);

  PermissionAssignmentPayload removeRolesFromSubject(List<String> roles, SubjectInput subject);

  List<Role> findRoles();

  List<BasePermissions> findPermissions();
}
