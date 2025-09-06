package com.simplecommerce.product;

import static com.simplecommerce.shared.Types.NODE_PRODUCT;
import static java.util.stream.Collectors.toMap;

import com.simplecommerce.shared.Actor;
import com.simplecommerce.shared.GlobalId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;
import reactor.core.publisher.Mono;

/**
 * Controller for products.
 *
 * @since 1.0
 * @author julius.krah
 */
@Controller
class ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final ObjectProvider<ProductService> productService;
    // TODO: Add search functionality when service layer is ready
    // private final SearchQueryParser searchQueryParser;
    // private final SearchQueryTranslator searchQueryTranslator;
    // TODO: Add state machine when service layer is ready
    // private final ProductStateMachineService stateMachineService;
    private final PriceResolutionService priceResolutionService;
    // Defer creation of the ProductService to avoid early initialization of aspectj proxy
    private final Supplier<ProductService> productServiceSupplier = SingletonSupplier.of(ProductManagement::new);

    ProductController(BatchLoaderRegistry registry, ObjectProvider<ProductService> productService, PriceResolutionService priceResolutionService) {
      this.productService = productService;
      this.priceResolutionService = priceResolutionService;
      registry.<String, List<String>>forName("tagsDataLoader")
          .registerMappedBatchLoader((productIds, env) -> {
              var keyContext = env.getKeyContextsList().getFirst();
              LOG.info("Fetching tags for {} product(s): {}", productIds.size(), productIds);
              return Mono.fromSupplier(() -> productService.getIfAvailable(productServiceSupplier).findTags(productIds, (Integer) keyContext)
                  .stream().map(productWithTags -> Map.entry(
                      productWithTags.getId().toString(), productWithTags.getTags()))
                  .collect(toMap(Entry::getKey, Entry::getValue))
              );
      });
    }

    @QueryMapping
    Product product(@Argument String id) {
        return productService.getIfAvailable(productServiceSupplier).findProduct(id);
    }

    @QueryMapping
    Window<Product> products(ScrollSubrange subrange, Sort sort, @Argument String query) {
        var limit = subrange.count().orElse(100);
        var scroll = subrange.position().orElse(ScrollPosition.keyset());
        
        LOG.info("Fetching {} products with scroll {} and query: {}", limit, scroll, query);
        LOG.debug("Sorting with: {{}}", sort);
        
        // Use search-enabled method that supports ANTLR query parsing
        return productService.getIfAvailable(productServiceSupplier).findProducts(limit, sort, scroll, query);
    }

    @SchemaMapping
    Window<Product> products(Category source, ScrollSubrange subrange, Sort sort, @Argument Boolean includeSubcategories) {
        var limit = subrange.count().orElse(100);
        var scroll = subrange.position().orElse(ScrollPosition.keyset());
        return productService.getIfAvailable(productServiceSupplier).findProductsByCategory(source.id(), limit, sort, scroll);
    }

    @SchemaMapping(typeName = "Product")
    String id(Product source) {
        return new GlobalId(NODE_PRODUCT, source.id()).encode();
    }

    @SchemaMapping
    CompletableFuture<List<String>> tags(
        Product product, @Argument int limit, DataLoader<String, List<String>> tagsDataLoader) {
        LOG.debug("Deferring fetching {} tag(s) for product: {}", limit, product.id());
        return tagsDataLoader.load(product.id(), limit);
    }

    @SchemaMapping(typeName = "Product")
    Optional<PriceRange> priceRange(Product source, Locale locale, @Argument String currency) {
        // Use provided currency or default to USD
        var currencyCode = currency != null ? currency : "USD";
        var priceContext = PriceContext.defaultContext(currencyCode);
        
        LOG.debug("Calculating price range for product {} with context {}", source.id(), priceContext);
        
        // Get the ProductEntity by decoding the global ID
        try {
            var gid = GlobalId.decode(source.id());
            var productEntity = new ProductEntity();
            productEntity.setId(UUID.fromString(gid.id()));
            
            return priceResolutionService.calculatePriceRange(productEntity, priceContext, locale);
        } catch (Exception e) {
            LOG.error("Error calculating price range for product {}: {}", source.id(), e.getMessage());
            return Optional.empty();
        }
    }

    @SchemaMapping(typeName = "Product")
    Optional<Actor> createdBy() {
        return Optional.empty();
    }

    @SchemaMapping(typeName = "Product")
    Optional<Actor> updatedBy() {
        return Optional.empty();
    }

    // PriceSet has been moved to ProductVariant level - removed from Product

    @MutationMapping
    String deleteProduct(@Argument String id) {
        var deletedId = productService.getIfAvailable(productServiceSupplier).deleteProduct(id);
        return new GlobalId(NODE_PRODUCT, deletedId).encode();
    }

    @MutationMapping
    Product updateProduct(@Argument String id, @Argument ProductInput input) {
        return productService.getIfAvailable(productServiceSupplier).updateProduct(id, input);
    }

    @MutationMapping
    Product addProduct(@Argument ProductInput input) {
        LOG.debug("Creating product: {}", input);
        return productService.getIfAvailable(productServiceSupplier).createProduct(input);
    }

    // TODO: Implement state machine mutations when service layer is ready
    /*
    @MutationMapping
    Product publishProduct(@Argument String id) {
        LOG.info("Publishing product: {}", id);
        // TODO: Implement product publishing with state machine validation
        throw new UnsupportedOperationException("Product state machine not yet implemented");
    }

    @MutationMapping
    Product archiveProduct(@Argument String id) {
        LOG.info("Archiving product: {}", id);
        // TODO: Implement product archiving with state machine validation
        throw new UnsupportedOperationException("Product state machine not yet implemented");
    }

    @MutationMapping
    Product reactivateProduct(@Argument String id) {
        LOG.info("Reactivating product: {}", id);
        // TODO: Implement product reactivation with state machine validation
        throw new UnsupportedOperationException("Product state machine not yet implemented");
    }
    */

}
