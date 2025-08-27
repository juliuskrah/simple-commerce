package com.simplecommerce.product;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.node.NodeServiceSupplier;
import com.simplecommerce.shared.Types;
import org.springframework.util.function.SingletonSupplier;

/**
 * @author julius.krah
 */
public class ProductVariantNodeServiceSupplier implements NodeServiceSupplier {
  private final SingletonSupplier<NodeService> productVariantNodeService = SingletonSupplier.of(ProductVariantManagement::new);

  @Override
  public NodeService getNodeService() {
    return productVariantNodeService.obtain();
  }

  @Override
  public boolean supports(String node) {
    return Types.NODE_PRODUCT_VARIANT.equals(node);
  }

}
