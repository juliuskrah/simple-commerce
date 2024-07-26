package com.simplecommerce.product;

import static com.simplecommerce.product.ProductManagement.NODE_PRODUCT;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.Money;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import static java.util.stream.Collectors.*;

import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.ArgumentValue;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Controller;
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
    private final ProductService productService;

    ProductController(ProductService productService, BatchLoaderRegistry registry) {
      this.productService = productService;

      registry.<String, List<String>>forName("tagsDataLoader")
          .registerMappedBatchLoader((productIds, env) -> {
              var ketContexts = env.getKeyContexts();
              LOG.info("Fetching tags for {} product(s)", productIds.size());
              return Mono.fromSupplier(() -> productIds.stream().collect(
              toMap(Function.identity(), productId ->
                  productService.findTags(productId, (Integer) ketContexts.get(productId)))));
      });
    }

    @QueryMapping
    Product product(@Argument String id) {
        return productService.findProduct(id);
    }

    @QueryMapping
    List<Product> products(@Argument ArgumentValue<Integer> first) {
        var limit = first.asOptional().orElse(100);
        LOG.info("Fetching {} products", limit);
        return productService.findProducts(limit);
    }

    @SchemaMapping(typeName = "Product")
    String id(Product source) {
        return new GlobalId(NODE_PRODUCT, source.id()).encode();
    }

    @SchemaMapping
    CompletableFuture<List<String>> tags(
        Product product, @Argument int first, DataLoader<String, List<String>> tagsDataLoader) {
        LOG.info("Deferring fetching {} tag(s) for product: {}", first, product.id());
        return tagsDataLoader.load(product.id(), first);
    }

    @SchemaMapping
    Money price(Product product) {
        return new Money("USD", new BigDecimal("100.00"));
    }

    @SchemaMapping
    List<URL> media(Product product) throws MalformedURLException {
        return List.of(URI.create("https://example.com/image.jpg").toURL());
    }

    @MutationMapping
    String deleteProduct(@Argument String id) {
        var deletedId = productService.deleteProduct(id);
        return new GlobalId(NODE_PRODUCT, deletedId).encode();
    }

    @MutationMapping
    Product updateProduct(@Argument String id, @Argument ProductInput input) {
        return new Product(
            id,
            input.title(),
            input.title().replace(" ", "-"),
            OffsetDateTime.now(),
            input.description(),
            OffsetDateTime.now()
        );
    }

    @MutationMapping
    Product addProduct(@Argument ProductInput input) {
        return productService.createProduct(input);
    }
}
