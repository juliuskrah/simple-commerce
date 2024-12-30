package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test the {@link CategoryController}.
 * @author julius.krah
 */
@GraphQlTest(CategoryController.class)
class CategoryControllerTest {
  @Autowired private GraphQlTester graphQlTester;
  @MockitoBean private CategoryService categoryService;

  @Test
  @DisplayName("Should fetch category by ID")
  void shouldFetchCategory() {
    when(categoryService.findCategory(anyString()))
        .thenReturn(new Category("39a0ec70-de93-4005-8c73-d32f36d4ae55", "Category", "category",
            null, null, null));
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryDetails")
        .execute()
        .path("category").entity(Category.class)
        .satisfies(category -> {
          assertThat(category).isNotNull()
              .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
              .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvMzlhMGVjNzAtZGU5My00MDA1LThjNzMtZDMyZjM2ZDRhZTU1");
          assertThat(category).extracting(Category::title).isEqualTo("Category");
        });
  }

  @Test
  @DisplayName("Should fetch category level by category ID")
  void shouldFetchCategoryWithLevel() {
    var id = "0e5f79b1-8aca-4634-a617-5c2d12ac6a5f";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    when(categoryService.findCategoryLevels(anySet())).thenReturn(Stream.of(3));
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryWithLevel")
        .execute()
        .path("category", category -> category.path("level").entity(Integer.class)
            .isEqualTo(3))
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvMGU1Zjc5YjEtOGFjYS00NjM0LWE2MTctNWMyZDEyYWM2YTVm"));
  }
}