package com.simplecommerce.shared.authorization;

public enum BaseRoles {
  ONLINE_STORE_EDITOR("Online store editor", BasePermissions.VIEW_DASHBOARD),
  CUSTOMER_SUPPORT("Customer support", BasePermissions.VIEW_DASHBOARD),
  MERCHANDISER("Merchandiser", BasePermissions.VIEW_PRODUCTS, BasePermissions.EDIT_PRODUCTS, BasePermissions.DELETE_PRODUCTS),
  MARKETER("Marketer", BasePermissions.VIEW_DASHBOARD),
  ;

  BaseRoles(String name, BasePermissions... permission) {
    this(name, "Store role", permission);
  }

  BaseRoles(String name, String roleCategory, BasePermissions... permissions) {
    this.name = name;
    this.roleCategory = roleCategory;
    this.permissions = permissions;
  }

  final String name;
  final String roleCategory;
  final BasePermissions[] permissions;

  public String getName() {
    return name;
  }

  public BasePermissions[] getPermissions() {
    return permissions;
  }
}
