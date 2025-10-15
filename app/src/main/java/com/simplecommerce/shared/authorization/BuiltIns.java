package com.simplecommerce.shared.authorization;

/**
 * This class contains built-in roles and permissions for the SimpleCommerce application. It serves as a centralized place to define and manage default authorization settings.
 */
public final class BuiltIns {

  private BuiltIns() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final BaseRoles[] DEFAULT_ROLES = BaseRoles.values();
  public static final BasePermissions[] DEFAULT_PERMISSIONS = BasePermissions.values();
}
