package com.simplecommerce.node;

import java.util.Map;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * @since 1.0
 * @author julius.krah
 */
@Controller
class NodeController {
  private final Map<String, NodeService> nodeServices;

  NodeController(Map<String, NodeService> nodeServices) {
    this.nodeServices = nodeServices;
  }

  @QueryMapping
  Node node(@Argument String id) {
    return nodeServices.get("Product").node(id);
  }
}
