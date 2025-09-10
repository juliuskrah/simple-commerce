package com.simplecommerce.product.pricing;

import com.simplecommerce.product.PriceRange;
import com.simplecommerce.product.Product;
import com.simplecommerce.product.ProductEntity;
import com.simplecommerce.product.variant.ProductVariantEntity;
import com.simplecommerce.product.variant.ProductVariants;
import com.simplecommerce.shared.utils.MonetaryUtils;
import com.simplecommerce.shared.types.Money;
import com.simplecommerce.shared.exceptions.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for resolving the appropriate price for a product variant
 * based on the given pricing context (geography, currency, customer group, etc.).
 *
 * @author julius.krah
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
public class PriceResolutionService {

  private static final Logger LOG = LoggerFactory.getLogger(PriceResolutionService.class);
  
  private final ProductVariants variantRepository;
  @PersistenceContext
  private EntityManager entityManager;

  public PriceResolutionService(ProductVariants variantRepository) {
    this.variantRepository = variantRepository;
  }

  /**
   * Resolves the price for a product variant based on the given context.
   * The resolution algorithm:
   * 1. Find all active price sets for the variant
   * 2. For each price set, find matching price rules based on context
   * 3. Select the rule from the highest priority price set
   * 4. If multiple rules match in the same price set, prefer more specific contexts
   * 5. Fall back to DEFAULT context rules if no specific match found
   *
   * @param variant the product variant
   * @param context the pricing context
   * @param locale the locale for currency formatting
   * @return the resolved price, or empty if no price can be determined
   */
  public Optional<Money> resolvePrice(ProductVariantEntity variant, @Nullable PriceContextInput context, Locale locale) {
    PriceContext ctx = PriceContext.defaultContext(locale);
    if(Objects.nonNull(context)) {
      ctx = new PriceContext(locale, context.customerGroup(), context.region(), context.currency(), context.quantity());
    }
    LOG.debug("Resolving price for variant {} with context {}", variant.getId(), ctx);

    var currentTime = OffsetDateTime.now();
    if(!entityManager.contains(variant)) {
      variant = variantRepository.findById(variant.getId()).orElseThrow(NotFoundException::new);
    }
    
    // Get all active price sets, ordered by priority (highest first)
    var activePriceSets = variant.getPriceSets().stream()
        .filter(PriceSetEntity::getActive)
        .sorted(Comparator.comparing(PriceSetEntity::getPriority).reversed())
        .toList();

    if (activePriceSets.isEmpty()) {
      LOG.warn("No active price sets found for variant {}", variant.getId());
      return Optional.empty();
    }

    // Try to find a matching price rule
    for (var priceSet : activePriceSets) {
      var matchingRule = findBestMatchingRule(priceSet.getRules(), ctx, currentTime);
      if (matchingRule.isPresent()) {
        var rule = matchingRule.get();
        var currency = MonetaryUtils.getCurrency(rule.getPriceCurrency(), locale);
        var resolvedPrice = new Money(currency, rule.getPriceAmount());
        
        LOG.debug("Resolved price {} for variant {} using rule {} from price set {}", 
            resolvedPrice, variant.getId(), rule.getId(), priceSet.getId());
        
        return Optional.of(resolvedPrice);
      }
    }

    LOG.warn("No matching price rule found for variant {} with context {}", variant.getId(), context);
    return Optional.empty();
  }

  /**
   * Finds the best matching price rule from a list of rules based on the given context.
   * Matching priority:
   * 1. Exact context match (GEOGRAPHIC, CURRENCY, CUSTOMER_GROUP with matching value)
   * 2. DEFAULT context (fallback)
   *
   * @param rules the list of price rules to search
   * @param context the pricing context
   * @param currentTime the current time for validity checking
   * @return the best matching rule, or empty if none found
   */
  private Optional<PriceRuleEntity> findBestMatchingRule(List<PriceRuleEntity> rules, 
                                                        PriceContext context, 
                                                        OffsetDateTime currentTime) {
    
    // Filter valid rules for the given context and time
    var validRules = rules.stream()
        .filter(rule -> rule.isValidFor(context, context.quantity(), currentTime))
        .toList();

    if (validRules.isEmpty()) {
      return Optional.empty();
    }

    // Priority 1: Find exact context matches (non-DEFAULT)
    var exactMatches = validRules.stream()
        .filter(rule -> rule.getContextType() != PriceContextType.DEFAULT)
        .toList();

    if (!exactMatches.isEmpty()) {
      // If multiple exact matches, prefer the first one (could be enhanced with more complex logic)
      return Optional.of(exactMatches.getFirst());
    }

    // Priority 2: Fall back to DEFAULT context
    var defaultMatches = validRules.stream()
        .filter(rule -> rule.getContextType() == PriceContextType.DEFAULT)
        .toList();

    return defaultMatches.isEmpty() ? Optional.empty() : Optional.of(defaultMatches.getFirst());
  }

  /**
   * Calculates the price range for a product based on all its variants' pricing contexts.
   * This considers all possible pricing scenarios to determine min/max prices.
   *
   * @param product the product entity
   * @param locale the locale for currency formatting
   * @return the price range, or empty if no prices can be determined
   */
  public Optional<PriceRange> calculatePriceRange(Product product, Locale locale) {
    LOG.debug("Calculating price range for product {}", product.id());
    var productEntity = new ProductEntity();
    productEntity.setId(UUID.fromString(product.id()));
    // Fetch all variants for the product
    var variants = variantRepository.findByProductId(
        productEntity.getId(),
        Limit.unlimited(),
        Sort.unsorted(),
        ScrollPosition.keyset()
    ).getContent();

    if (variants.isEmpty()) {
      LOG.debug("No variants found for product {}", product.id());
      return Optional.empty();
    }

    // Collect all prices by resolving pricing for each variant
    var prices = variants.stream()
        .map(variant -> resolvePrice(variant, null, locale))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    if (prices.isEmpty()) {
      LOG.debug("No resolved prices found for product {} variants", product.id());
      return Optional.empty();
    }

    // Find min and max prices
    var minPrice = prices.stream()
        .min(Comparator.comparing(Money::amount));
    
    var maxPrice = prices.stream()
        .max(Comparator.comparing(Money::amount));

    return minPrice.isPresent() && maxPrice.isPresent() 
        ? Optional.of(new PriceRange(minPrice.get(), maxPrice.get()))
        : Optional.empty();
  }
}