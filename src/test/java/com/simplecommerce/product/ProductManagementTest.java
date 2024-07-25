package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

/**
 * @author julius.krah
 */
@ExtendWith(MockitoExtension.class)
class ProductManagementTest {
  @Mock
  private Products productRepository;
  @InjectMocks
  ProductManagement productService;

  @Test
  void shouldResolveNode() {
    var entity = new ProductEntity();
    entity.setId(UUID.fromString("1c7e429c-45ed-45d7-9d1a-36f561b9d6b9"));
    entity.setTitle("Kaspersky");
    when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/Product/1c7e429c-45ed-45d7-9d1a-36f561b9d6b9
    var product = productService.node("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8xYzdlNDI5Yy00NWVkLTQ1ZDctOWQxYS0zNmY1NjFiOWQ2Yjk=");
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "1c7e429c-45ed-45d7-9d1a-36f561b9d6b9")
        .hasFieldOrPropertyWithValue("title", "Kaspersky");
  }

  @Test
  void shouldFindProduct() {
    var entity = new ProductEntity();
    entity.setId(UUID.fromString("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c"));
    entity.setTitle("Cyber Sphere");
    when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/Product/7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c
    var product = productService.findProduct("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC83MDA0ZWJiYy1lNzFjLTQ1ZjMtOGQyMy0xYmEyYzM3ZjJmMWM=");
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Cyber Sphere");
  }

  @Test
  void shouldFindProducts() {
    var entity = new ProductEntity();
    entity.setId(UUID.randomUUID());
    entity.setTitle("Opti Core");
    when(productRepository.findBy(any(Limit.class))).thenReturn(List.of(entity));
    var products = productService.findProducts(1);

    var expected = new Product(null, "Opti Core", null, null, null, null);
    assertThat(products).isNotNull().hasSize(1)
        .usingRecursiveComparison().comparingOnlyFields("title")
        .isEqualTo(List.of(expected));
  }

  @Test
  void shouldFindTags() {
    var spyTags = spy(new ArrayList<String>());
    spyTags.add("security");
    spyTags.add("collaboration");
    final var limit = 10;
    doReturn(limit).when(spyTags).size();
    when(productRepository.findTags(any(UUID.class), any(Limit.class))).thenReturn(spyTags);
    var tags = productService.findTags("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c", limit);
    assertThat(tags).isNotNull().isNotEmpty().hasSize(limit)
        .contains("security", "collaboration");
  }

  @Test
  void shouldCreateProduct() {
    when(productRepository.saveAndFlush(any(ProductEntity.class))).thenAnswer((invocation) -> {
      ProductEntity productEntity = invocation.getArgument(0);
      productEntity.setId(UUID.fromString("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c"));
      return null; // don't care about the return value
    });
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
        new BigDecimal("123.90"), List.of("security"), "Cyber Sphere");
    Product product = productService.createProduct(productInput);
    assertThat(product).isNotNull()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Cyber Sphere")
        .hasFieldOrPropertyWithValue("slug", "cyber-sphere");
  }

  @Test
  void shouldDeleteProduct() {
    // gid://SimpleCommerce/Product/9dfef6b8-0fae-4fcd-b51f-69e4de5a5b3e
    var id = productService.deleteProduct("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC85ZGZlZjZiOC0wZmFlLTRmY2QtYjUxZi02OWU0ZGU1YTViM2U=");
    assertThat(id).isNotNull().isEqualTo("9dfef6b8-0fae-4fcd-b51f-69e4de5a5b3e");
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