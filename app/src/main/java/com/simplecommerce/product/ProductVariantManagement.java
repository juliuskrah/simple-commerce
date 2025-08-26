package com.simplecommerce.product;

import static com.simplecommerce.shared.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.VirtualThreadHelper.runInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Supplier;
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
 * Service implementation for managing product variants.
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class ProductVariantManagement implements ProductVariantService, NodeService {

  private ProductVariants variantRepository;

  public void setVariantRepository(ObjectFactory<ProductVariants> variantRepository) {
    this.variantRepository = variantRepository.getObject();
  }

  private ProductVariantEntity toEntity(UUID productId, ProductVariantInput input) {
    var entity = new ProductVariantEntity();
    var product = new ProductEntity();
    product.setId(productId);
    entity.setProduct(product);
    entity.setSku(input.sku());
    entity.setTitle(input.title());
    entity.setSystemGenerated(false);
    
    if (input.price() != null) {
      entity.setPriceAmount(input.price().amount());
      entity.setPriceCurrency(input.price().currency());
    }
    
    return entity;
  }

  private ProductVariant fromEntity(ProductVariantEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new ProductVariant(
        entity.getId().toString(),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getLastModifiedDate().orElseGet(epoch),
        entity.getProduct().getId().toString(),
        entity.getSku(),
        entity.getTitle()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public ProductVariant node(String id) {
    var gid = GlobalId.decode(id);
    var variant = callInScope(() -> variantRepository.findById(UUID.fromString(gid.id())));
    return variant.map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductVariant findVariant(String id) {
    var gid = GlobalId.decode(id);
    var variant = callInScope(() -> variantRepository.findById(UUID.fromString(gid.id())));
    return variant.map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public Window<ProductVariant> findVariantsByProduct(String productId, int limit, Sort sort, ScrollPosition scroll) {
    return callInScope(() -> variantRepository.findByProductId(UUID.fromString(productId), Limit.of(limit), sort, scroll))
        .map(this::fromEntity);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public ProductVariant createVariant(String productId, ProductVariantInput input) {
    var gid = GlobalId.decode(productId);
    var productUuid = UUID.fromString(gid.id());
    var variantEntity = toEntity(productUuid, input);
    
    // Check if this is the first user variant - if so, delete the system-generated default
    var defaultVariant = callInScope(() -> variantRepository.findByProductIdAndSystemGenerated(productUuid, true));
    defaultVariant.ifPresent(productVariantEntity -> runInScope(() -> variantRepository.deleteById(productVariantEntity.getId())));
    
    runInScope(() -> variantRepository.saveAndFlush(variantEntity));
    return fromEntity(variantEntity);
  }

  @Override
  public ProductVariant updateVariant(String id, ProductVariantInput input) {
    var gid = GlobalId.decode(id);
    var variantId = UUID.fromString(gid.id());
    
    return callInScope(() -> variantRepository.findById(variantId))
        .map(entity -> {
          entity.setSku(input.sku());
          entity.setTitle(input.title());
          
          if (input.price() != null) {
            entity.setPriceAmount(input.price().amount());
            entity.setPriceCurrency(input.price().currency());
          } else {
            entity.setPriceAmount(null);
            entity.setPriceCurrency(null);
          }
          
          return fromEntity(entity);
        })
        .orElseThrow(NotFoundException::new);
  }

  @Override
  public String deleteVariant(String id) {
    var gid = GlobalId.decode(id);
    var variantId = UUID.fromString(gid.id());
    
    // Get the variant to check the product
    var variant = callInScope(() -> variantRepository.findById(variantId))
        .orElseThrow(NotFoundException::new);
    
    // Check if this is the last variant for the product
    var variantCount = callInScope(() -> variantRepository.countByProductId(variant.getProduct().getId()));
    if (variantCount <= 1) {
      throw new IllegalStateException("Cannot delete the last variant of a product");
    }
    
    runInScope(() -> variantRepository.deleteById(variantId));
    return gid.id();
  }

  /**
   * Create a default variant for a product.
   * This is called when a product is created.
   */
  public ProductVariant createDefaultVariant(String productId, String productTitle, String productSlug, MoneyInput price) {
    var entity = new ProductVariantEntity();
    var product = new ProductEntity();
    product.setId(UUID.fromString(productId));
    entity.setProduct(product);
    
    // Generate unique SKU based on product slug
    var baseSku = productSlug + "-default";
    var sku = generateUniqueSku(baseSku);
    
    entity.setSku(sku);
    entity.setTitle(productTitle);
    entity.setSystemGenerated(true);
    
    if (price != null) {
      entity.setPriceAmount(price.amount());
      entity.setPriceCurrency(price.currency());
    }
    
    runInScope(() -> variantRepository.saveAndFlush(entity));
    return fromEntity(entity);
  }

  private String generateUniqueSku(String baseSku) {
    if (!callInScope(() -> variantRepository.findBySku(baseSku)).isPresent()) {
      return baseSku;
    }
    
    int counter = 1;
    while (true) {
      String candidateSku = baseSku + "-" + counter;
      if (!callInScope(() -> variantRepository.findBySku(candidateSku)).isPresent()) {
        return candidateSku;
      }
      counter++;
    }
  }
}
