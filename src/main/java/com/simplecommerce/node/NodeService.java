package com.simplecommerce.node;

/**
 * Implementations of this interface are represented managed node types that participate in
 * a relay connection.
 * @since 1.0
 * @author julius.krah
 */
public interface NodeService {

  /**
   * Find a node by its ID. The polymorphic type is determined by the structure of the ID. <br/>
   * e.g. {@code gid://SimpleCommerce/{nodeType}/{id}}
   * @param id The ID of the node.
   * @return The node.
   */
  Node node(String id);
}
