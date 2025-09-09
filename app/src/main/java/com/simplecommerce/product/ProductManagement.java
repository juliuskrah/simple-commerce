package com.simplecommerce.product;

import static com.simplecommerce.shared.utils.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.runInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.product.ProductEvent.ProductEventType;
import com.simplecommerce.product.search.SearchQueryParser;
import com.simplecommerce.product.search.SearchQueryTranslator;
import com.simplecommerce.shared.Event;
import com.simplecommerce.shared.types.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.Slug;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

  private Products productRepository;
  private Event<ProductEvent> event;
  private ProductVariants variantRepository;
  private SearchQueryParser searchQueryParser;
  private SearchQueryTranslator searchQueryTranslator;

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
        entity.getId().toString(),
        entity.getTitle(),
        entity.getSlug(),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getDescription(),
        entity.getLastModifiedDate().orElseGet(epoch),
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
    return callInScope( () -> productRepository.findBy(Limit.of(limit), sort, scroll))
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
    
    // Execute search with specification 
    // TODO: Implement proper cursor-based pagination with specifications
    // For now, fall back to simple pagination when search is used
    LOG.warn("Search with specifications enabled but cursor pagination not yet implemented. Using simple approach.");
    
    var products = callInScope(() -> productRepository.findAll(specification, sort));
    var limitedProducts = products.stream()
        .limit(limit)
        .map(this::fromEntity)
        .toList();
    
    // Return a basic Window without proper cursor pagination
    // This is a temporary implementation until proper pagination is added
    return Window.from(limitedProducts, pos -> null);
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
  @Override
  public String deleteProduct(String id) {
    var gid = GlobalId.decode(id);
    runInScope(() -> productRepository.deleteById(UUID.fromString(gid.id())));
    var entity = new ProductEntity();
    entity.setId(UUID.fromString(gid.id()));
    event.fire(new ProductEvent(entity, ProductEventType.DELETED));
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
      variant.setPriceCurrency("USD"); // Default to USD
    }
    
    runInScope(() -> variantRepository.saveAndFlush(variant));
  }

  /**
   * {@inheritDoc}
   */
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
  @Override
  public Product mapToProduct(ProductEntity productEntity) {
    return fromEntity(productEntity);
  }
}
