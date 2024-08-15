package com.simplecommerce.product;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.node.NodeServiceSupplier;
import com.simplecommerce.shared.Types;

/**
 * @author julius.krah
 */
public class ProductNodeServiceSupplier implements NodeServiceSupplier {
  private NodeService productNodeService;

  @Override
  public NodeService getNodeService() {
    if (productNodeService != null) {
      return productNodeService;
    }
    productNodeService = new ProductManagement();
    return productNodeService;
  }

  @Override
  public boolean supports(String node) {
    return Types.NODE_PRODUCT.equals(node);
  }

}
