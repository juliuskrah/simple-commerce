package com.simplecommerce.shared;

/**
 * Implementations of this interface are represented managed node types that participate in
 * a relay connection.
 * @since 1.0
 * @author julius.krah
 */
public interface NodeService {
  Node node(String id);
}
