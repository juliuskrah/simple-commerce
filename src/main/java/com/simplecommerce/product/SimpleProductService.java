package com.simplecommerce.product;

import static com.simplecommerce.product.SimpleProductService.NODE_PRODUCT;

import com.simplecommerce.node.Node;
import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.Slug;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * @author julius.krah
 */
@Service(NODE_PRODUCT)
class SimpleProductService implements ProductService, NodeService {
  static final String NODE_PRODUCT = "Product";

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

  /**
   * {@inheritDoc}
   */
  @Override
  public String deleteProduct(String id) {
    // decode id
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Product createProduct(ProductInput product) {
    String slug = Slug.generate(product.title());
    return new Product(
        "3",
        product.title(),
        slug,
        OffsetDateTime.now(),
        product.description(),
        product.tags(),
        OffsetDateTime.now()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Product updateProduct(String productId, ProductInput product) {
    return null;
  }
}
