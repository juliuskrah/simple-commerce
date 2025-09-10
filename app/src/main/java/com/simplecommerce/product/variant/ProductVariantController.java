package com.simplecommerce.product.variant;

import static com.simplecommerce.shared.types.Types.NODE_PRODUCT_VARIANT;

import com.simplecommerce.product.Product;
import com.simplecommerce.product.ProductStatus;
import com.simplecommerce.product.pricing.PriceContextInput;
import com.simplecommerce.product.pricing.PriceResolutionService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.utils.MonetaryUtils;
import com.simplecommerce.shared.types.Money;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * Controller for product variants and related operations.
 *
 * @author julius.krah
 * @since 1.0
 */
@Controller
class ProductVariantController {

  private final ObjectProvider<ProductVariantService> variantService;
  private final Supplier<ProductVariantService> variantServiceSupplier = SingletonSupplier.of(ProductVariantManagement::new);
  private final PriceResolutionService priceResolutionService;

  ProductVariantController(ObjectProvider<ProductVariantService> variantService, PriceResolutionService priceResolutionService) {
    this.variantService = variantService;
    this.priceResolutionService = priceResolutionService;
  }

  @QueryMapping
  ProductVariant variant(@Argument String id) {
    return variantService.getIfAvailable(variantServiceSupplier).findVariant(GlobalId.decode(id).id());
  }

  @SchemaMapping
  Window<ProductVariant> variants(Product source, ScrollSubrange subrange, Sort sort) {
    var limit = subrange.count().orElse(100);
    var scroll = subrange.position().orElse(ScrollPosition.keyset());
    return variantService.getIfAvailable(variantServiceSupplier).findVariantsByProduct(source.id(), limit, sort, scroll);
  }

  @SchemaMapping(typeName = "ProductVariant")
  String id(ProductVariant source) {
    return new GlobalId(NODE_PRODUCT_VARIANT, source.id()).encode();
  }

  @SchemaMapping(typeName = "ProductVariant")
  Product product(ProductVariant source) {
    // TODO: Return a minimal Product record for the variant's product
    return new Product(source.productId(), null, null, null, null, null, ProductStatus.DRAFT);
  }

  @SchemaMapping(typeName = "ProductVariant")
  Optional<Money> price(ProductVariant source, Locale locale) {
    if (source.priceAmount() == null || source.priceCurrency() == null) {
      return Optional.empty();
    }
    var currency = MonetaryUtils.getCurrency(source.priceCurrency(), locale);
    return Optional.of(new Money(currency, source.priceAmount()));
  }

  @SchemaMapping(typeName = "ProductVariant")
  Optional<Money> resolvedPrice(ProductVariant source, @Argument PriceContextInput context, Locale locale) {
    var variant = new ProductVariantEntity();
    variant.setId(UUID.fromString(source.id()));
    return priceResolutionService.resolvePrice(variant, context, locale);
  }

  @MutationMapping
  ProductVariant addProductVariant(@Argument String productId, @Argument ProductVariantInput input) {
    return variantService.getIfAvailable(variantServiceSupplier).createVariant(productId, input);
  }

  @MutationMapping
  ProductVariant updateProductVariant(@Argument String id, @Argument ProductVariantInput input) {
    return variantService.getIfAvailable(variantServiceSupplier).updateVariant(id, input);
  }

  @MutationMapping
  String deleteProductVariant(@Argument String id) {
    var deletedId = variantService.getIfAvailable(variantServiceSupplier).deleteVariant(id);
    return new GlobalId(NODE_PRODUCT_VARIANT, deletedId).encode();
  }
}
