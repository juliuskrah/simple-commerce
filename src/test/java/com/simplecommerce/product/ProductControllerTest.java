package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.ExceptionHandling;
import com.simplecommerce.shared.NotFoundException;
import com.simplecommerce.shared.config.Sorting;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test for {@link ProductController}.
 * @author julius.krah
 * @since 1.0
 */
@Import({Sorting.class, ExceptionHandling.class})
@GraphQlTest(ProductController.class)
class ProductControllerTest {
  @Autowired
  private GraphQlTester graphQlTester;
  @MockitoBean
  private ProductService productService;

  @Test
  @DisplayName("Should fetch product by ID")
  void shouldFetchProduct() {
    when(productService.findProduct(anyString()))
        .thenReturn(new Product("18d25652-5870-4555-8146-5166fec97c3f", "Product", "product",
            null, null, null));
    graphQlTester.documentName("productDetails")
        .variable("id", "gid://SimpleCommerce/Product/some-random-id-1234567")
        .operationName("productDetails")
        .execute()
        .path("product").entity(Product.class)
        .satisfies( product -> {
            assertThat(product).isNotNull()
                .extracting(Product::id, as(InstanceOfAssertFactories.STRING)).isBase64()
                .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8xOGQyNTY1Mi01ODcwLTQ1NTUtODE0Ni01MTY2ZmVjOTdjM2Y=");
            assertThat(product).extracting(Product::title).isEqualTo("Product");
        });
  }

  @Test
  @DisplayName("Should not fetch product that does not exist")
  void shouldNotFetchUnknownProduct() {
    when(productService.findProduct(anyString()))
        .thenThrow(new NotFoundException());
    graphQlTester.documentName("productDetails")
        .variable("id", "gid://SimpleCommerce/Product/some-random-id-1234567")
        .operationName("productDetails")
        .execute().errors()
        .expect(error -> error.getErrorType() == ErrorType.NOT_FOUND)
        .satisfy( errors -> assertThat(errors).hasSize(1).first()
            .extracting(ResponseError::getExtensions, ResponseError::getMessage, ResponseError::getPath)
            .containsExactly(Map.of(), "Cannot be found", "product"))
        .path("product").valueIsNull();
  }

  @Test
  @DisplayName("Should fetch tags by product ID")
  void shouldFetchProductWithTags() {
    var id = "c6f56e4a-bb2e-4ca2-b267-ea398ae8cb34";
    ArgumentCaptor<Integer> firstCaptor = ArgumentCaptor.captor();
    when(productService.findProduct(anyString()))
        .thenReturn(new Product(id, "Cyber Cafe", "cyber-cafe",
            null, null, null));
    when(productService.findTags(anySet(), firstCaptor.capture()))
        .thenReturn(List.of(new ProductWithTags() {
          @Override
          public UUID getId() {
            return UUID.fromString(id);
          }

          @Override
          public List<String> getTags() {
            return List.of("game", "software");
          }
        }));

    var first = 5;
    graphQlTester.documentName("productDetails")
        .variable("id", "gid://SimpleCommerce/Product/some-random-id-1234567")
        .variable("first", first)
        .operationName("productWithTags")
        .execute()
        .path("product", product -> product.path("tags").entityList(String.class)
            .satisfies(tags -> assertThat(tags).isNotEmpty()
                .contains("game", "software")))
        .entity(Product.class).satisfies(product -> assertThat(product).isNotNull()
            .extracting(Product::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC9jNmY1NmU0YS1iYjJlLTRjYTItYjI2Ny1lYTM5OGFlOGNiMzQ="));

    assertThat(firstCaptor.getValue()).isEqualTo(first);
  }

  @Test
  @DisplayName("Should fetch product with price set")
  void shouldFetchProductWithPriceSet() {
    when(productService.findProduct(anyString())).thenReturn(
        new Product("c6f56e4a-bb2e-4ca2-b267-ea398ae8cb34", "Cyber Cafe", "cyber-cafe",
        null, null, null));
    var entity = new ParameterizedTypeReference<Map<String, Object>>() {};
    graphQlTester.documentName("productDetails")
        .variable("id", "gid://SimpleCommerce/Product/some-random-id-1234567")
        .operationName("productWithPrice")
        .execute()
        .path("product", product -> product.path("priceSet")
            .entity(entity).satisfies(
                priceSet -> assertThat(priceSet).isNotNull()
                    .hasFieldOrProperty("id")
                    .hasFieldOrProperty("prices")
            ).path("priceRange")
            .entity(entity).satisfies(
                priceRange -> assertThat(priceRange).isNotNull()
                    .hasFieldOrProperty("start")
                    .hasFieldOrProperty("stop")
            )
        );
  }

  @Test
  @DisplayName("Should fetch products")
  void shouldFetchProducts() {
    var entities = List.of(new Product(UUID.randomUUID().toString(), "Cyberdyne Rover", "cyberdyne-rover",
        null, null, null));
    when(productService.findProducts(anyInt(), any(Sort.class), any(ScrollPosition.class)))
        .thenReturn(Window.from(entities, ignored -> ScrollPosition.keyset()));
    graphQlTester.documentName("products")
        .variable("first", 10)
        .execute()
        .path("products.edges[*].node").entityList(Product.class)
        .satisfies(products -> assertThat(products).isNotEmpty()
            .hasSize(1)
            .extracting(Product::title).contains("Cyberdyne Rover"));
  }

  @Test
  @DisplayName("Should add a product")
  void shouldAddProduct() {
    when(productService.createProduct(any(ProductInput.class)))
        .thenReturn(new Product("1", "Product 1", "product-1",
            null, null, null));
    graphQlTester.documentName("addProduct")
        .variable("input", Map.of("title", "Product 1"))
        .execute()
        .path("addProduct").entity(Product.class)
        .satisfies(product -> {
            assertThat(product).isNotNull();
            assertThat(product).extracting(Product::title, Product::slug)
                .containsExactly("Product 1", "product-1");
        });
  }

  @Test
  @DisplayName("Should update a product by ID")
  void shouldUpdateProduct() {
    when(productService.updateProduct(anyString(), any(ProductInput.class)))
        .thenReturn(new Product("1", "Neu Garne", "neu-garne",
            null, null, null));
    graphQlTester.documentName("updateProduct")
        .variable("id", "1")
        .variable("input", Map.of("title", "Neu Garne"))
        .execute()
        .path("updateProduct").entity(Product.class)
        .satisfies(product -> {
            assertThat(product).isNotNull();
            assertThat(product)
                .extracting(Product::title, Product::slug)
                .containsExactly("Neu Garne", "neu-garne");
        });
  }

  @Test
  @DisplayName("Should delete a product by ID")
  void shouldDeleteProduct() {
    when(productService.deleteProduct(anyString())).thenReturn("12345");
    graphQlTester.documentName("deleteProduct")
        .variable("id", "12345")
        .executeAndVerify();
  }
}
