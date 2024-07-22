package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author julius.krah
 */
class ProductManagementTest {
  final ProductManagement productService = new ProductManagement();

  @Test
  void shouldResolveNode() {
    // gid://SimpleCommerce/Product/1c7e429c-45ed-45d7-9d1a-36f561b9d6b9
    var product = productService.node("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8xYzdlNDI5Yy00NWVkLTQ1ZDctOWQxYS0zNmY1NjFiOWQ2Yjk=");
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "1c7e429c-45ed-45d7-9d1a-36f561b9d6b9")
        .hasFieldOrPropertyWithValue("title", "Product");
  }

  @Test
  void shouldFindProduct() {
    // gid://SimpleCommerce/Product/7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c
    var product = productService.findProduct("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC83MDA0ZWJiYy1lNzFjLTQ1ZjMtOGQyMy0xYmEyYzM3ZjJmMWM=");
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Product");
  }

  @Test
  void shouldFindProducts() {
    var products = productService.findProducts(10);
    assertThat(products).isNotNull().isEmpty();
  }

  @Test
  void shouldCreateProduct() {
    var productInput = new ProductInput(
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
    Product product = productService.createProduct(productInput);
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Cyber Sphere")
        .hasFieldOrPropertyWithValue("slug", "cyber-sphere");
  }

  @Test
  void shouldDeleteProduct() {
    var id = productService.deleteProduct("1");
    assertThat(id).isNotNull().isEqualTo("1");
  }

  @Test
  void shouldUpdateProduct() {
    var productInput = new ProductInput("""
        InnoGrid is a revolutionary digital platform designed to power innovation and
        streamline complex workflows for businesses of all sizes. At its core, InnoGrid
        offers a versatile grid computing environment that allows for scalable processing
        power and data storage, making it ideal for data-intensive applications. With its
        robust suite of tools, including real-time analytics, machine learning integrations,
        and collaborative workspaces, InnoGrid enables teams to harness the power of collective
        intelligence and accelerate their project timelines. The platform's intuitive interface
        ensures ease of use, while its high-level security features protect your valuable data,
        giving you peace of mind as you innovate and grow.
        """, new BigDecimal("123.90"), List.of(), "InnoGrid");
    Product product = productService.updateProduct("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC82ZmQwZGFkYS1jMDk1LTRlODgtYjhkNy03OTE2YTk3ZTc5NTg=", productInput);
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "6fd0dada-c095-4e88-b8d7-7916a97e7958")
        .hasFieldOrPropertyWithValue("title", "InnoGrid");
  }
}