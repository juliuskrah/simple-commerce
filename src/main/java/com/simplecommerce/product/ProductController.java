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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.ArgumentValue;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

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

    ProductController(ProductService productService) {
      this.productService = productService;
    }

    @QueryMapping
    Product product(@Argument String id) {
        return productService.findProduct(id);
    }

    @QueryMapping
    List<Product> products(@Argument ArgumentValue<Integer> first) {
        LOG.info("Fetching {} products", first.value());
        return List.of(
            new Product(
                "1",
                "Product 1",
                "product-1",
                OffsetDateTime.now(),
                "Product 1 description",
                List.of("tag1", "tag2"),
                OffsetDateTime.now()
            ),
            new Product(
                "2",
                "Product 2",
                "product-2",
                OffsetDateTime.now(),
                "Product 2 description",
                List.of("tag1", "tag2"),
                OffsetDateTime.now()
            )
        );
    }

    @SchemaMapping(typeName = "Product")
    String id(Product source) {
        return new GlobalId(NODE_PRODUCT, source.id()).encode();
    }

    @SchemaMapping
    Money price(Product product) {
        return new Money("USD", new BigDecimal("100.00"));
    }

    @SchemaMapping
    List<URL> media(Product product) throws MalformedURLException {
        return List.of(URL.of(URI.create("https://example.com/image.jpg"), null));
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
            input.tags(),
            OffsetDateTime.now()
        );
    }

    @MutationMapping
    Product addProduct(@Argument ProductInput input) {
        return productService.createProduct(input);
    }
}
