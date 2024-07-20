package com.simplecommerce.product;

import static com.simplecommerce.product.SimpleProductService.NODE_PRODUCT;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.Slug;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author julius.krah
 */
@Service(NODE_PRODUCT)
@Transactional
class SimpleProductService implements ProductService, NodeService {
  static final String NODE_PRODUCT = "Product";

  @Transactional(readOnly = true)
  @Override
  public Product node(String id) {
    return findProduct(id);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public Product findProduct(String id) {
    var gid = GlobalId.decode(id);
    return new Product(
        gid.id(),
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
  @Transactional(readOnly = true)
  @Override
  public List<Product> findProducts(int limit) {
    return List.of();
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
        "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c",
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
    var gid = GlobalId.decode(productId);
    // Fetch product by ID. When not found, throw an exception
    return new Product(
        gid.id(),
        product.title(),
        null,
        OffsetDateTime.now(),
        product.description(),
        product.tags(),
        OffsetDateTime.now());
  }
}
