package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

/**
 * Test for {@link ProductController}.
 * @author julius.krah
 * @since 1.0
 */
@GraphQlTest(ProductController.class)
class ProductControllerTest {
  @Autowired
  private GraphQlTester graphQlTester;
  @MockBean
  private ProductService productService;

  @Test
  @DisplayName("Should fetch product by ID")
  void shouldFetchProduct() {
    when(productService.findProduct(anyString()))
        .thenReturn(new Product("18d25652-5870-4555-8146-5166fec97c3f", "Product", "product",
            null, null, null));
    graphQlTester.documentName("productDetails")
       .variable("id", "gid://SimpleCommerce/Product/some-random-id-1234567")
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
  @DisplayName("Should fetch products")
  void shouldFetchProducts() {
    var entities = List.of(new Product(UUID.randomUUID().toString(), "Cyberdyne Rover", "cyberdyne-rover",
        null, null, null));
    when(productService.findProducts(anyInt())).thenReturn(entities);
    graphQlTester.documentName("products")
        .execute()
        .path("products").entityList(Product.class)
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
    graphQlTester.documentName("updateProduct")
        .execute()
        .path("updateProduct").entity(Product.class)
        .satisfies(product -> {
            assertThat(product).isNotNull();
            assertThat(product).extracting(Product::title).isEqualTo("Product 1");
        });
  }

  @Test
  @DisplayName("Should delete a product by ID")
  void shouldDeleteProduct() {
    when(productService.deleteProduct(anyString())).thenReturn("12345");
    graphQlTester.documentName("deleteProduct")
        .variable("id", "12345")
        .execute()
        .path("deleteProduct").entity(String.class)
        .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8xMjM0NQ==");
  }

  @Test
  @DisplayName("Should fetch tags by product ID")
  void shouldFetchProductTags() {

  }
}
