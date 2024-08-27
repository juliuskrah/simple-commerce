package com.simplecommerce.product;

import static com.simplecommerce.shared.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.VirtualThreadHelper.runInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import com.simplecommerce.shared.Slug;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
class ProductManagement implements ProductService, NodeService {

  public void setProductRepository(ObjectFactory<Products> productRepository) {
    this.productRepository = productRepository.getObject();
  }

  private Products productRepository;

  private ProductEntity toEntity(ProductInput product) {
    var entity = new ProductEntity();
    entity.setTitle(product.title());
    entity.setSlug(Slug.generate(product.title()));
    entity.setDescription(product.description());
    entity.setTags(product.tags());
    return entity;
  }

  private Product fromEntity(ProductEntity entity) {
    return new Product(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getSlug(),
        entity.getCreatedAt(),
        entity.getDescription(),
        entity.getUpdatedAt()
    );
  }

  @Transactional(propagation = Propagation.NESTED)
  private Optional<ProductEntity> updateProduct(ProductInput product, String productId) {
    var gid = GlobalId.decode(productId);
    return productRepository.findById(UUID.fromString(gid.id()))
        .map(productEntity -> {
          productEntity.setTitle(product.title());
          productEntity.setDescription(product.description());
          productEntity.setTags(product.tags());
          return productEntity;
        });
  }

  /**
   * {@inheritDoc}
   */
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
    var product = callInScope(() -> productRepository.findById(UUID.fromString(gid.id())));
    return product.map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> findTags(String productId, int limit) {
    return callInScope(() -> productRepository.findTags(UUID.fromString(productId), Limit.of(limit)));
  }

  @Override
  public List<ProductWithTags> findTags(Set<String> productIds, int limit) {
    var ids = productIds.stream().map(UUID::fromString).collect(Collectors.toSet());
    return callInScope(() -> productRepository.findTags(limit, ids));
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public Window<Product> findProducts(int limit, Sort sort, ScrollPosition scroll) {
    return callInScope( () -> productRepository.findBy(Limit.of(limit), sort, scroll))
        .map(this::fromEntity);
  }

  /**
   * {@inheritDoc}
   */
  @PreAuthorize("denyAll()")
  @Override
  public String deleteProduct(String id) {
    var gid = GlobalId.decode(id);
    runInScope(() -> productRepository.deleteById(UUID.fromString(gid.id())));
    return gid.id();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Product createProduct(ProductInput product) {
    var productEntity = toEntity(product);
    productEntity.publishProductCreatedEvent();
    runInScope(() -> productRepository.saveAndFlush(productEntity));
    return fromEntity(productEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Product updateProduct(String productId, ProductInput product) {
    return callInScope(() -> updateProduct(product, productId))
        .map(this::fromEntity)
        .orElseThrow(NotFoundException::new);
  }
}
