package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
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

  @Test
  @DisplayName("Should fetch product by ID")
  void shouldFetchProduct() {
    graphQlTester.documentName("productDetails")
       .variable("id", "12345")
        .execute()
        .path("product").entity(Product.class)
        .satisfies( product -> {
            assertThat(product).isNotNull()
                .extracting(Product::id, as(InstanceOfAssertFactories.STRING)).isBase64()
                .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8xMjM0NQ==");
            assertThat(product).extracting(Product::title).isEqualTo("Product");
        });
  }

  @Test
  @DisplayName("Should fetch products")
  void shouldFetchProducts() {
    graphQlTester.documentName("products")
        .execute()
        .path("products").entityList(Product.class)
        .satisfies(products -> {
            assertThat(products).isNotEmpty();
            assertThat(products).hasSize(2);
            assertThat(products).extracting(Product::title).contains("Product 1", "Product 2");
        });
  }

  @Test
  @DisplayName("Should add a product")
  void shouldAddProduct() {
    graphQlTester.documentName("addProduct")
        .execute()
        .path("addProduct").entity(Product.class)
        .satisfies(product -> {
            assertThat(product).isNotNull();
            assertThat(product).extracting(Product::title).isEqualTo("Product 1");
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
    graphQlTester.documentName("deleteProduct")
        .execute()
        .path("deleteProduct").entity(String.class)
        .isEqualTo("Product deleted");
  }
}
