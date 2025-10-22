package com.simplecommerce.shared.authorization;

import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.DASHBOARD_NAMESPACE;
import static com.simplecommerce.shared.authorization.BasePermissions.Namespaces.PRODUCT_NAMESPACE;

public enum BasePermissions {
  VIEW_PRODUCTS(PRODUCT_NAMESPACE, "view"),
  VIEW_PRODUCT_COSTS(PRODUCT_NAMESPACE, "view_cost"),
  CREATE_AND_EDIT_PRODUCTS(PRODUCT_NAMESPACE, "edit"),
  EDIT_PRODUCT_COSTS(PRODUCT_NAMESPACE, "edit_cost"),
  EDIT_PRODUCT_PRICES(PRODUCT_NAMESPACE, "edit_price"),
  EXPORT_PRODUCTS(PRODUCT_NAMESPACE, "export"),
  DELETE_PRODUCTS(PRODUCT_NAMESPACE, "delete"),
  VIEW_DASHBOARD(DASHBOARD_NAMESPACE, "viewers"),
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

  public interface Namespaces {
    String ACTOR_NAMESPACE = "Actor";
    String CATEGORY_NAMESPACE = "Category";
    String DASHBOARD_NAMESPACE = "Dashboard";
    String GROUP_NAMESPACE = "Group";
    String ORDER_NAMESPACE = "Order";
    String PRODUCT_NAMESPACE = "Product";
    String PRODUCT_VARIANT_NAMESPACE = "ProductVariant";
    String ROLE_NAMESPACE = "Role";
  }
}
