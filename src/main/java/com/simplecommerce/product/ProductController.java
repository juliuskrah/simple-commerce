package com.simplecommerce.product;

import static com.simplecommerce.shared.Types.NODE_PRODUCT;
import static java.util.stream.Collectors.toMap;

import com.simplecommerce.shared.Actor;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    // Defer creation of the ProductService to avoid early initialization of aspectj proxy
    private final Supplier<ProductService> productServiceSupplier = SingletonSupplier.of(ProductManagement::new);

    ProductController(BatchLoaderRegistry registry, ObjectProvider<ProductService> productService) {
      this.productService = productService;
      registry.<String, List<String>>forName("tagsDataLoader")
          .registerMappedBatchLoader((productIds, env) -> {
              var ketContext = env.getKeyContextsList().getFirst();
              LOG.info("Fetching tags for {} product(s): {}", productIds.size(), productIds);
              return Mono.fromSupplier(() -> productService.getIfAvailable(productServiceSupplier).findTags(productIds, (Integer) ketContext)
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
    Window<Product> products(ScrollSubrange subrange, Sort sort) {
        var limit = subrange.count().orElse(100);
        var scroll = subrange.position().orElse(ScrollPosition.keyset());
        LOG.info("Fetching {} products with scroll {}", limit, scroll);
        LOG.debug("Sorting with: {{}}", sort);
        return productService.getIfAvailable(productServiceSupplier).findProducts(limit, sort, scroll);
    }

    @SchemaMapping(typeName = "Product")
    String id(Product source) {
        return new GlobalId(NODE_PRODUCT, source.id()).encode();
    }

    @SchemaMapping
    CompletableFuture<List<String>> tags(
        Product product, @Argument int limit, DataLoader<String, List<String>> tagsDataLoader) {
        LOG.info("Deferring fetching {} tag(s) for product: {}", limit, product.id());
        return tagsDataLoader.load(product.id(), limit);
    }

    @SchemaMapping
    PriceRange priceRange(Product product) {
        return new PriceRange(
            new Money("USD", new BigDecimal("100.00")),
            new Money("USD", new BigDecimal("200.00"))
        );
    }

    @SchemaMapping
    Actor createdBy(Product product) {
        return null;
    }

    @SchemaMapping
    Actor updatedBy(Product product) {
        return null;
    }

    @SchemaMapping
    PriceSet priceSet(Product product) {
        return null;
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
        return productService.getIfAvailable(productServiceSupplier).createProduct(input);
    }

}
