package com.simplecommerce.product;

import static com.simplecommerce.shared.authorization.BasePermissions.VIEW_PRODUCTS;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.runInScope;
import static java.util.Objects.requireNonNull;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.product.ProductEvent.ProductEventType;
import com.simplecommerce.product.category.CategoryEntity;
import com.simplecommerce.product.search.SearchQueryParser;
import com.simplecommerce.product.search.SearchQueryTranslator;
import com.simplecommerce.product.variant.ProductVariantEntity;
import com.simplecommerce.product.variant.ProductVariants;
import com.simplecommerce.security.aspects.Check;
import com.simplecommerce.security.aspects.Permit;
import com.simplecommerce.shared.Event;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.authorization.BasePermissions.Namespaces;
import com.simplecommerce.shared.authorization.KetoAuthorizationService;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.exceptions.OperationNotAllowedException;
import com.simplecommerce.shared.types.Product;
import com.simplecommerce.shared.types.ProductStatus;
import com.simplecommerce.shared.utils.Slug;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
class ProductManagement implements ProductService, NodeService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductManagement.class);

  public void setProductRepository(ObjectFactory<Products> productRepository) {
    this.productRepository = productRepository.getObject();
  }
  public void setEvent(ObjectFactory<Event<ProductEvent>> event) {
    this.event = event.getObject();
  }
  public void setVariantRepository(ObjectFactory<ProductVariants> variantRepository) {
    this.variantRepository = variantRepository.getObject();
  }
  public void setSearchQueryParser(ObjectFactory<SearchQueryParser> searchQueryParser) {
    this.searchQueryParser = searchQueryParser.getObject();
  }
  public void setSearchQueryTranslator(ObjectFactory<SearchQueryTranslator> searchQueryTranslator) {
    this.searchQueryTranslator = searchQueryTranslator.getObject();
  }
  public void setKetoAuthorizationService(ObjectFactory<KetoAuthorizationService> ketoAuthorizationService) {
    this.ketoAuthorizationService = ketoAuthorizationService.getObject();
  }
  public void setStateMachineService(ObjectFactory<ProductStateMachineService> stateMachineService) {
    this.stateMachineService = stateMachineService.getObject();
  }

  private Products productRepository;
  private Event<ProductEvent> event;
  private ProductVariants variantRepository;
  private SearchQueryParser searchQueryParser;
  private SearchQueryTranslator searchQueryTranslator;
  private KetoAuthorizationService ketoAuthorizationService;
  private ProductStateMachineService stateMachineService;

  private ProductEntity toEntity(ProductInput product) {
    var category = new CategoryEntity();
    category.setId(UUID.fromString("6ef9c5ce-0430-468e-8adb-523fc05c4a11")); // Uncategorized
    var entity = new ProductEntity();
    entity.setTitle(product.title());
    entity.setSlug(Slug.generate(product.title()));
    entity.setDescription(product.description());
    entity.setTags(product.tags());
    entity.setCategory(category);
    return entity;
  }

  private Product fromEntity(ProductEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new Product(
        requireNonNull(entity.getId()).toString(),
        entity.getTitle(),
        entity.getSlug(),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getCreatedBy().orElse("system"),
        entity.getDescription(),
        entity.getLastModifiedDate().orElseGet(epoch),
        entity.getLastModifiedBy().orElse("system"),
        entity.getStatus() != null ? entity.getStatus() : ProductStatus.DRAFT
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
  @Check(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "view", returnObject = "returnObject.status", object = "T(com.simplecommerce.shared.GlobalId).decode(#id).id")
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
  @Transactional(readOnly = true)
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
    boolean hasPermission = false;
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      hasPermission = ketoAuthorizationService.checkPermission(Namespaces.PRODUCT_NAMESPACE, "__LIST__", VIEW_PRODUCTS.getPermission(), authentication.getName());
    }
    // Limit to published products if no permission
    Specification<ProductEntity> spec = hasPermission ? Specification.unrestricted() : (root, _, cb) ->
        cb.equal(root.get("status"), ProductStatus.PUBLISHED);
    return callInScope(() -> productRepository.findBy(spec, function ->
        function.sortBy(sort).limit(limit).scroll(scroll)))
        .map(this::fromEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public Window<Product> findProducts(int limit, Sort sort, ScrollPosition scroll, String searchQuery) {
    if (searchQuery == null || searchQuery.trim().isEmpty()) {
      // Delegate to the simple version if no search query
      return findProducts(limit, sort, scroll);
    }

    LOG.debug("Finding products with search query: {}", searchQuery);
    
    // Parse the search query
    var parsedQuery = searchQueryParser.parse(searchQuery);
    LOG.debug("Parsed search query: {}", parsedQuery);
    
    // Translate to JPA Specification
    var specification = searchQueryTranslator.translateToSpecification(parsedQuery);

    // For now, fall back to simple pagination when search is used
    LOG.warn("Search with specifications enabled but cursor pagination not yet implemented. Using simple approach.");
    
    var products = callInScope(() -> productRepository.findAll(specification, sort));
    var limitedProducts = products.stream()
        .limit(limit)
        .map(this::fromEntity)
        .toList();
    
    // Return a basic Window without proper cursor pagination
    // This is a temporary implementation until proper pagination is added
    return Window.from(limitedProducts, _ -> null);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public Window<Product> findProductsByCategory(String categoryId, int limit, Sort sort, ScrollPosition scroll) {
    return callInScope(() -> productRepository.findByCategoryId(UUID.fromString(categoryId), Limit.of(limit), sort, scroll))
        .map(this::fromEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Permit(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "delete", object = "T(com.simplecommerce.shared.GlobalId).decode(#id).id")
  @Override
  public String deleteProduct(String id) {
    var gid = GlobalId.decode(id);
    variantRepository.deleteByProductId(UUID.fromString(gid.id()));
    productRepository.deleteById(UUID.fromString(gid.id()));
    var entity = new ProductEntity();
    entity.setId(UUID.fromString(gid.id()));
    event.fire(new ProductEvent(entity, ProductEventType.DELETED));
    return gid.id();
  }

  /**
   * {@inheritDoc}
   */
  @Permit(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "edit", object = "'__CREATE__'")
  @Override
  public Product createProduct(ProductInput product) {
    var productEntity = toEntity(product);
    runInScope(() -> productRepository.saveAndFlush(productEntity));
    
    // Create default variant
    createDefaultVariant(productEntity, product);
    
    return fromEntity(productEntity);
  }

  private void createDefaultVariant(ProductEntity productEntity, ProductInput productInput) {
    var variant = new ProductVariantEntity();
    variant.setProduct(productEntity);
    variant.setSku(productEntity.getSlug() + "-default");
    variant.setTitle(productEntity.getTitle());
    variant.setSystemGenerated(true);
    
    // Set price from product input if provided
    if (productInput.price() != null) {
      variant.setPriceAmount(productInput.price());
      variant.setPriceCurrency("EUR"); // Default to EUR
    }
    
    runInScope(() -> variantRepository.saveAndFlush(variant));
  }

  /**
   * {@inheritDoc}
   */
  @Permit(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "edit", object = "T(com.simplecommerce.shared.GlobalId).decode(#productId).id")
  @Override
  public Product updateProduct(String productId, ProductInput product) {
    return callInScope(() -> updateProduct(product, productId))
        .map(entity -> {
          event.fire(new ProductEvent(entity, ProductEventType.UPDATED));
          return fromEntity(entity);
        })
        .orElseThrow(NotFoundException::new);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public ProductEntity findProductEntity(String id) {
    try {
      var gid = GlobalId.decode(id);
      return callInScope(() -> productRepository.findById(UUID.fromString(gid.id())).orElse(null));
    } catch (Exception e) {
      LOG.warn("Failed to find product entity with id: {}", id, e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ProductEntity saveProductEntity(ProductEntity productEntity) {
    return callInScope(() -> productRepository.saveAndFlush(productEntity));
  }

  /**
   * {@inheritDoc}
   */
  @Permit(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "edit", object = "T(com.simplecommerce.shared.GlobalId).decode(#id).id")
  @Override
  public Mono<Product> publishProduct(String id) {
    LOG.info("Publishing product: {}", id);

    var product = findProduct(id);

    return stateMachineService.publishProduct(product)
        .switchIfEmpty(Mono.error(new OperationNotAllowedException("Product state transition to PUBLISHED was rejected")))
        .publishOn(Schedulers.boundedElastic())
        .map(newStatus -> {
          var productEntity = findProductEntity(id);
          productEntity.setStatus(newStatus);
          return productEntity;
        }).<ProductEntity>handle((entity, sink) -> sink.next(productRepository.saveAndFlush(entity)))
        .map(entity -> {
          LOG.info("Successfully published product: {}", id);
          return fromEntity(entity);
        }).contextCapture();
  }

  /**
   * {@inheritDoc}
   */
  @Permit(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "edit", object = "T(com.simplecommerce.shared.GlobalId).decode(#id).id")
  @Override
  public Mono<Product> archiveProduct(String id) {
    LOG.info("Archiving product: {}", id);

    var product = findProduct(id);

    return stateMachineService.archiveProduct(product)
        .switchIfEmpty(Mono.error(new OperationNotAllowedException("Product state transition to ARCHIVED was rejected")))
        .publishOn(Schedulers.boundedElastic())
        .map(newStatus -> {
          var productEntity = findProductEntity(id);
          productEntity.setStatus(newStatus);
          return productEntity;
        }).<ProductEntity>handle((entity, sink) -> sink.next(productRepository.saveAndFlush(entity)))
        .map(entity -> {
          LOG.info("Successfully archived product: {}", id);
          return fromEntity(entity);
        }).contextCapture();
  }

  /**
   * {@inheritDoc}
   */
  @Permit(namespace = Namespaces.PRODUCT_NAMESPACE, relation = "edit", object = "T(com.simplecommerce.shared.GlobalId).decode(#id).id")
  @Override
  public Mono<Product> reactivateProduct(String id) {
    LOG.info("Reactivating product: {}", id);

    var product = findProduct(id);

    return stateMachineService.reactivateProduct(product)
        .switchIfEmpty(Mono.error(new OperationNotAllowedException("Product state transition to DRAFT was rejected")))
        .publishOn(Schedulers.boundedElastic())
        .map(newStatus -> {
          var productEntity = findProductEntity(id);
          productEntity.setStatus(newStatus);
          return productEntity;
        }).<ProductEntity>handle((entity, sink) -> sink.next(productRepository.saveAndFlush(entity)))
        .map(entity -> {
          LOG.info("Successfully reactivated product: {}", id);
          return fromEntity(entity);
        });
  }
}
