package com.simplecommerce.shared.authorization;

public enum BasePermissions {
  VIEW_PRODUCTS("Product", "viewers"),
  EDIT_PRODUCTS("Product", "editors"),
  DELETE_PRODUCTS("Product", "owners"),
  VIEW_DASHBOARD("Dashboard", "viewers"),
  ;

  BasePermissions(String namespace, String permission) {
    this.namespace = namespace;
    this.permission = permission;
  }

  final String namespace;
  final String permission;

  public String getNamespace() {
    return namespace;
  }

  public String getPermission() {
    return permission;
  }
}
