package com.simplecommerce.product;

import static com.simplecommerce.shared.types.Types.NODE_PRODUCT;
import static java.util.stream.Collectors.toMap;

import com.simplecommerce.actor.Actor;
import com.simplecommerce.product.category.Category;
import com.simplecommerce.product.pricing.PriceResolutionService;
import com.simplecommerce.shared.GlobalId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
    private final ProductStateMachineService stateMachineService;
    private final PriceResolutionService priceResolutionService;
    // Defer creation of the ProductService to avoid early initialization of aspectj proxy
    private final Supplier<ProductService> productServiceSupplier = SingletonSupplier.of(ProductManagement::new);

    ProductController(BatchLoaderRegistry registry, ObjectProvider<ProductService> productService, 
                      PriceResolutionService priceResolutionService, ProductStateMachineService stateMachineService) {
      this.productService = productService;
      this.priceResolutionService = priceResolutionService;
      this.stateMachineService = stateMachineService;
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
    Optional<PriceRange> priceRange(Product source, Locale locale) {
        LOG.debug("Calculating price range for product {} from locale {}", source.id(), locale);
        
        try {
            return priceResolutionService.calculatePriceRange(source, locale);
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

    @MutationMapping
    Product publishProduct(@Argument String id) {
        LOG.info("Publishing product: {}", id);
        
        var service = productService.getIfAvailable(productServiceSupplier);
        var productEntity = service.findProductEntity(id);
        
        if (productEntity == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        
        // Validate business rules before attempting state transition
        if (!stateMachineService.canPublish(productEntity)) {
            throw new IllegalStateException("Product cannot be published: validation failed");
        }
        
        boolean success = stateMachineService.publishProduct(productEntity);
        if (!success) {
            throw new IllegalStateException("Failed to publish product: state transition rejected");
        }
        
        // Save the updated product status
        productEntity = service.saveProductEntity(productEntity);
        
        LOG.info("Successfully published product: {}", id);
        return service.mapToProduct(productEntity);
    }

    @MutationMapping
    Product archiveProduct(@Argument String id) {
        LOG.info("Archiving product: {}", id);
        
        var service = productService.getIfAvailable(productServiceSupplier);
        var productEntity = service.findProductEntity(id);
        
        if (productEntity == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        
        boolean success = stateMachineService.archiveProduct(productEntity);
        if (!success) {
            throw new IllegalStateException("Failed to archive product: state transition rejected");
        }
        
        // Save the updated product status
        productEntity = service.saveProductEntity(productEntity);
        
        LOG.info("Successfully archived product: {}", id);
        return service.mapToProduct(productEntity);
    }

    @MutationMapping
    Product reactivateProduct(@Argument String id) {
        LOG.info("Reactivating product: {}", id);
        
        var service = productService.getIfAvailable(productServiceSupplier);
        var productEntity = service.findProductEntity(id);
        
        if (productEntity == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        
        boolean success = stateMachineService.reactivateProduct(productEntity);
        if (!success) {
            throw new IllegalStateException("Failed to reactivate product: state transition rejected");
        }
        
        // Save the updated product status
        productEntity = service.saveProductEntity(productEntity);
        
        LOG.info("Successfully reactivated product: {}", id);
        return service.mapToProduct(productEntity);
    }

}
