package com.simplecommerce.actor;

import com.simplecommerce.shared.authorization.BasePermissions;
import com.simplecommerce.shared.types.PermissionTupleInput;
import com.simplecommerce.shared.types.Role;
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

  User findUser(String username);

  Optional<Actor> addPermissionsToActor(String username, List<PermissionTupleInput> permissions);

  Optional<Actor> removePermissionsFromActor(String username, List<PermissionTupleInput> permissions);

  List<Role> findRoles();

  List<BasePermissions> findPermissions();
}
