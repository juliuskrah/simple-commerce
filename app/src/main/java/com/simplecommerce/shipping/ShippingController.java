package com.simplecommerce.shipping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * Controller for shipping operations.
 *
 * @author julius.krah
 * @since 1.0
 */
@Controller
class ShippingController {

  private static final Logger LOG = LoggerFactory.getLogger(ShippingController.class);

  private final ObjectProvider<ShippingService> shippingService;
  private final Supplier<ShippingService> shippingServiceSupplier =
      SingletonSupplier.of(ShippingManagement::new);

  ShippingController(ObjectProvider<ShippingService> shippingService) {
    this.shippingService = shippingService;
  }

  @QueryMapping
  List<ShippingMethod> availableShippingMethods(
      @Argument String country,
      @Argument String state,
      @Argument BigDecimal orderAmount) {

    LOG.debug("Getting available shipping methods: country={}, state={}, amount={}",
        country, state, orderAmount);

    return shippingService.getIfAvailable(shippingServiceSupplier)
        .getAvailableShippingMethods(country, state, orderAmount);
  }

  @QueryMapping
  Optional<ShippingMethod> shippingMethod(@Argument String id) {
    LOG.debug("Fetching shipping method: id={}", id);
    try {
      return Optional.of(shippingService.getIfAvailable(shippingServiceSupplier)
          .getShippingMethod(id));
    } catch (Exception e) {
      LOG.warn("Shipping method not found: {}", id, e);
      return Optional.empty();
    }
  }

  @SchemaMapping(typeName = "ShippingMethod")
  String id(ShippingMethod source) {
    return source.id();
  }
}
