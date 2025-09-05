package com.simplecommerce.node;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.simplecommerce.file.MediaFile;
import com.simplecommerce.product.Category;
import com.simplecommerce.product.Product;
import com.simplecommerce.product.ProductStatus;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;

/**
 * @author julius.krah
 */
@GraphQlTest(NodeController.class)
class NodeControllerTest {
  @Autowired
  private GraphQlTester graphQlTester;
  @Mock
  private NodeServiceSupplier nodeServiceSupplier;

  private MockedStatic<NodeServiceSupplier> nodeServiceSupplierMock;

  @BeforeEach
  void setUp() {
    nodeServiceSupplierMock = mockStatic(NodeServiceSupplier.class);
  }

  @AfterEach
  void tearDown() {
    nodeServiceSupplierMock.close();
  }

  @Test
  @DisplayName("Should resolve product node")
  void shouldResolveProductNode() {
    when(nodeServiceSupplier.get()).thenReturn(id -> new Product(id, "Product", "product", null, "{}", null, ProductStatus.DRAFT));
    nodeServiceSupplierMock.when(() -> NodeServiceSupplier.findFirst(any()))
        .thenReturn(Optional.of(nodeServiceSupplier));
    graphQlTester.documentName("nodeDetails")
        .variable("id", "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC9kYjU5MTMxNy1iMzhjLTQyMGYtYWIzOC00ZDM0ZDRlNDc3YjE=")
        .execute()
        .path("node", node -> node.path("__typename").entity(String.class).isEqualTo("Product"))
        .entity(Product.class)
        .satisfies(product -> {
          assertThat(product).isNotNull()
              .extracting(Product::id, as(InstanceOfAssertFactories.STRING)).isBase64()
              .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC9kYjU5MTMxNy1iMzhjLTQyMGYtYWIzOC00ZDM0ZDRlNDc3YjE=");
          assertThat(product).extracting(Product::title, Product::slug).containsExactly("Product", "product");
        });
  }

  @Test
  @DisplayName("Should resolve category node")
  void shouldResolveCategoryNode() {
    when(nodeServiceSupplier.get()).thenReturn(id -> new Category(id, "Category", "category", null, "{}", null));
    nodeServiceSupplierMock.when(() -> NodeServiceSupplier.findFirst(any()))
        .thenReturn(Optional.of(nodeServiceSupplier));
    graphQlTester.documentName("nodeDetails")
        .variable("id", "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvZmQ3MTI3MDctNDlkNy00MjBmLTkyMTctOTk3ZTk3OTNlMWJl")
        .execute()
        .path("node", node -> node.path("__typename").entity(String.class).isEqualTo("Category"))
        .entity(Category.class)
        .satisfies(category -> {
          assertThat(category).isNotNull()
              .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
              .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvZmQ3MTI3MDctNDlkNy00MjBmLTkyMTctOTk3ZTk3OTNlMWJl");
          assertThat(category).extracting(Category::title, Category::slug).containsExactly("Category", "category");
        });
  }

  @Test
  @DisplayName("Should resolve media file node")
  void shouldResolveMediaFileNode() throws MalformedURLException {
    var url = URI.create("https://localhost/image/picture.png").toURL();
    when(nodeServiceSupplier.get()).thenReturn(id -> new MediaFile(id, null, "image/png", url, null));
    nodeServiceSupplierMock.when(() -> NodeServiceSupplier.findFirst(any()))
        .thenReturn(Optional.of(nodeServiceSupplier));
    graphQlTester.documentName("nodeDetails")
        .variable("id", "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvTWVkaWFGaWxlL2Y2MTYxNzc3LTc5MzQtNDJiNy05MTJkLTRmNDAyMGY0YjBmNg==")
        .execute()
        .path("node", node -> node.path("__typename").entity(String.class).isEqualTo("MediaFile"))
        .entity(MediaFile.class)
        .satisfies(mediaFile -> {
          assertThat(mediaFile).isNotNull()
              .extracting(MediaFile::id, as(InstanceOfAssertFactories.STRING)).isBase64()
              .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvTWVkaWFGaWxlL2Y2MTYxNzc3LTc5MzQtNDJiNy05MTJkLTRmNDAyMGY0YjBmNg==");
          assertThat(mediaFile).extracting(MediaFile::url, MediaFile::contentType).containsExactly(url, "image/png");
        });
  }
}