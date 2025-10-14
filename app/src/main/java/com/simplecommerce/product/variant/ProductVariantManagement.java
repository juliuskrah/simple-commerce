package com.simplecommerce.product.variant;

import static com.simplecommerce.shared.utils.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.utils.VirtualThreadHelper.runInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.product.pricing.PriceContextType;
import com.simplecommerce.product.pricing.PriceRuleEntity;
import com.simplecommerce.product.pricing.PriceSetEntity;
import com.simplecommerce.product.ProductEntity;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
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
  private static final String DEFAULT_PRICE_SET_NAME = "Default Pricing";

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
      entity.setPriceCurrency(input.price().currency().getCurrencyCode());
      
      // Create default price set and rule for contextual pricing
      createDefaultPriceSet(entity, input.price());
    }
    
    return entity;
  }

  private ProductVariant fromEntity(ProductVariantEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new ProductVariant(
        entity.getId() != null ? entity.getId().toString() : null,
        entity.getCreatedDate().orElseGet(epoch),
        entity.getLastModifiedDate().orElseGet(epoch),
        entity.getProduct() != null && entity.getProduct().getId() != null ? entity.getProduct().getId().toString() : null,
        entity.getSku(),
        entity.getTitle(),
        entity.getPriceAmount(),
        entity.getPriceCurrency()
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
    var variant = callInScope(() -> variantRepository.findById(UUID.fromString(id)));
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
            entity.setPriceCurrency(input.price().currency().getCurrencyCode());
            
            // Update or create default price set and rule for contextual pricing
            updateOrCreateDefaultPriceSet(entity, input.price());
          } else {
            entity.setPriceAmount(null);
            entity.setPriceCurrency(null);
            
            // Remove default price set if price is removed
            removeDefaultPriceSet(entity);
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
    if (variantCount <= 1L) {
      throw new IllegalStateException("Cannot delete the last variant of a product");
    }
    
    runInScope(() -> variantRepository.deleteById(variantId));
    return gid.id();
  }

  /**
   * Creates a default price set and rule for simple pricing scenarios.
   * This ensures compatibility with the contextual pricing system.
   */
  private void createDefaultPriceSet(ProductVariantEntity variant, MoneyInput price) {
    var priceSet = new PriceSetEntity();
    priceSet.setVariant(variant);
    priceSet.setName(DEFAULT_PRICE_SET_NAME);
    priceSet.setPriority(0);
    priceSet.setActive(true);

    setPriceRule(price, priceSet);
    variant.addPriceSet(priceSet);
  }

  /**
   * Updates or creates the default price set for a variant.
   */
  private void updateOrCreateDefaultPriceSet(ProductVariantEntity variant, MoneyInput price) {
    // Find existing default price set
    var defaultPriceSet = variant.getPriceSets().stream()
        .filter(ps -> DEFAULT_PRICE_SET_NAME.equals(ps.getName()))
        .findFirst();

    if (defaultPriceSet.isPresent()) {
      // Update existing default price rule
      var priceSet = defaultPriceSet.get();
      var defaultRule = priceSet.getRules().stream()
          .filter(rule -> rule.getContextType() == PriceContextType.DEFAULT)
          .findFirst();

      if (defaultRule.isPresent()) {
        var rule = defaultRule.get();
        rule.setPriceAmount(price.amount());
        rule.setPriceCurrency(price.currency().getCurrencyCode());
      } else {
        // Create new default rule if it doesn't exist
        setPriceRule(price, priceSet);
      }
    } else {
      // Create new default price set
      createDefaultPriceSet(variant, price);
    }
  }

  private void setPriceRule(MoneyInput price, PriceSetEntity priceSet) {
    var priceRule = new PriceRuleEntity();
    priceRule.setPriceSet(priceSet);
    priceRule.setContextType(PriceContextType.DEFAULT);
    priceRule.setContextValue(null);
    priceRule.setPriceAmount(price.amount());
    priceRule.setPriceCurrency(price.currency().getCurrencyCode());
    priceRule.setMinQuantity(1);
    priceRule.setMaxQuantity(null);
    priceRule.setValidFrom(null);
    priceRule.setValidUntil(null);
    priceSet.addRule(priceRule);
  }

  /**
   * Removes the default price set from a variant.
   */
  private void removeDefaultPriceSet(ProductVariantEntity variant) {
    variant.getPriceSets().removeIf(ps -> DEFAULT_PRICE_SET_NAME.equals(ps.getName()));
  }
}
