package com.simplecommerce.product;

import com.simplecommerce.node.Node;
import com.simplecommerce.node.NodeService;
import com.simplecommerce.node.NodeServiceSupplier;
import com.simplecommerce.shared.Types;
import org.springframework.util.function.SingletonSupplier;

/**
 * @author julius.krah
 */
public class CategoryNodeServiceSupplier implements NodeServiceSupplier {
  private final SingletonSupplier<NodeService> categoryNodeService = SingletonSupplier.of(CategoryManagement::new);

  @Override
  public NodeService getNodeService() {
    return categoryNodeService.obtain();
  }

  @Override
  public boolean supports(String node) {
    return Types.NODE_CATEGORY.equals(node);
  }
}
