package com.simplecommerce.node;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.types.Node;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * @since 1.0
 * @author julius.krah
 */
@Controller
class NodeController {

  @QueryMapping
  Node node(@Argument String id) {
    var gid = GlobalId.decode(id);
    NodeService nodeService = NodeServiceSupplier.findFirst(supplier -> supplier.supports(gid.node()))
        .map(NodeServiceSupplier::get)
        .orElseThrow(NotFoundException::new);
    return nodeService.node(id);
  }
}
