package com.simplecommerce.product;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.node.NodeServiceSupplier;
import com.simplecommerce.shared.Types;
import org.springframework.util.function.SingletonSupplier;

/**
 * @author julius.krah
 */
public class ProductNodeServiceSupplier implements NodeServiceSupplier {
  private final SingletonSupplier<NodeService> productNodeService = SingletonSupplier.of(ProductManagement::new);

  @Override
  public NodeService getNodeService() {
    return productNodeService.obtain();
  }

  @Override
  public boolean supports(String node) {
    return Types.NODE_PRODUCT.equals(node);
  }

}
