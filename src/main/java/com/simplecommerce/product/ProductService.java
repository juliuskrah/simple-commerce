package com.simplecommerce.product;

import com.simplecommerce.node.Node;
import com.simplecommerce.node.NodeService;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * @author julius.krah
 */
@Service("Product")
public class ProductService implements NodeService {

  @Override
  public Node node(String id) {
    return new Product(
        id,
        "Product",
        "product",
        OffsetDateTime.now(),
        "Product description",
        null,
        OffsetDateTime.now()
    );
  }
}
