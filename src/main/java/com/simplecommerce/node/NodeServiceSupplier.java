package com.simplecommerce.node;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @since 1.0
 * @author julius.krah
 */
@FunctionalInterface
public interface NodeServiceSupplier extends Supplier<NodeService> {

  @Override
  default NodeService get() {
    return getNodeService();
  }

  default boolean supports(String node) {
    return false;
  }

  NodeService getNodeService();

  static Optional<NodeServiceSupplier> findFirst(Predicate<NodeServiceSupplier> predicate) {
    return ServiceLoader.load(NodeServiceSupplier.class)
        .stream()
        .map(Provider::get)
        .filter(predicate)
        .findFirst();
  }
}
