package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author julius.krah
 */
class SimpleProductServiceTest {

  @Test
  void shouldCreateProduct() {
    ProductInput product = new ProductInput(
            """
            CyberSphere redefines the digital workspace by offering an integrated
            platform that combines top-tier security, real-time collaboration,
            and powerful data management capabilities. Whether you're a small business, a
            growing startup, or an individual user, CyberSphere provides the tools you need
            to thrive in the digital age. With features such as encrypted file sharing,
            comprehensive analytics, and customizable workflow automation, CyberSphere helps
            you stay ahead of the curve, enhancing productivity and ensuring your data is both
            accessible and protected.
            """,
        new BigDecimal("123.90"), List.of(), "Cyber Sphere");
    SimpleProductService productService = new SimpleProductService();
    Product createdProduct = productService.createProduct(product);
    assertThat(createdProduct).isNotNull()
        .hasFieldOrPropertyWithValue("id", "3")
        .hasFieldOrPropertyWithValue("slug", "cyber-sphere");
  }
}