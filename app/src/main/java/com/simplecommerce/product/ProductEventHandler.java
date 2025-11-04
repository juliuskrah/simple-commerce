package com.simplecommerce.product;

import static java.util.Objects.requireNonNull;

import com.simplecommerce.shared.authorization.AuthorizationBridge;
import com.simplecommerce.shared.authorization.BaseRoles;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Component
@Profile("keto-authz")
class ProductEventHandler {

  private final AuthorizationBridge authorizationBridge;

  ProductEventHandler(AuthorizationBridge authorizationBridge) {
    this.authorizationBridge = authorizationBridge;
  }


  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).CREATED")
  void assignActorPermission(ProductEvent event) {
    var source = event.source();
    source.getCreatedBy().ifPresent(actor ->
        authorizationBridge.assignActorPermissionOnProducts(actor, List.of(source.getId().toString()), "owners"));
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).CREATED")
  void assignMerchandiserRolePermission(ProductEvent event) {
    var source = event.source();
    authorizationBridge.assignRolePermissionOnProducts(
        BaseRoles.MERCHANDISER.getName(), List.of(requireNonNull(source.getId()).toString()), "owners");
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).CREATED")
  void assignAdministratorRolePermission(ProductEvent event) {
    var source = event.source();
    authorizationBridge.assignRolePermissionOnProducts(
        BaseRoles.ADMINISTRATOR.getName(), List.of(requireNonNull(source.getId()).toString()), "owners");
  }

  @ApplicationModuleListener(condition = "#event.eventType == T(com.simplecommerce.product.ProductEvent.ProductEventType).DELETED")
  void purgeProductRelations(ProductEvent event) {
    var source = event.source();
    authorizationBridge.purgeProductRelations(requireNonNull(source.getId()).toString());
  }
}
