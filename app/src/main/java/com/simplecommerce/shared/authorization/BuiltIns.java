package com.simplecommerce.shared.authorization;

import java.util.List;
import java.util.stream.Stream;

/**
 * This class contains built-in roles and permissions for the SimpleCommerce application. It serves as a centralized place to define and manage default authorization settings.
 */
public final class BuiltIns {

  private BuiltIns() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final List<BaseRoles> DEFAULT_ROLES = Stream.of(BaseRoles.values()).toList();
  public static final List<BasePermissions> DEFAULT_PERMISSIONS = Stream.of(BasePermissions.values()).toList();
}
