package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
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
    when(categoryService.findCategoryLevels(anySet())).thenReturn(List.of(3));
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

  @Test
  @DisplayName("Should fetch category parent by category ID")
  void shouldFetchCategoryParent() {
    var id = "f102eb90-1faf-4951-9baa-4414e665913c";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    var entity = new Category(UUID.randomUUID().toString(), "Parent", null, null, null, null);
    when(categoryService.findCategoryParent(anyString())).thenReturn(Optional.of(entity));
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryWithParent")
        .execute()
        .path("category", category -> category.path("parent").entity(Category.class)
            .satisfies(parent -> assertThat(parent).isNotNull()
                .extracting(Category::title).isEqualTo("Parent")))
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvZjEwMmViOTAtMWZhZi00OTUxLTliYWEtNDQxNGU2NjU5MTNj"));
  }

  @Test
  @DisplayName("Should fetch category no with parent by category ID")
  void shouldFetchCategoryWithoutParent() {
    var id = "aa4a9b48-09a5-4d0c-9543-bb88e917eab1";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    when(categoryService.findCategoryParent(anyString())).thenReturn(Optional.empty());
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryWithParent")
        .execute()
        .path("category", category -> category.path("parent").valueIsNull())
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id).isNotNull());
  }

  @Test
  @DisplayName("Should fetch category ancestors by category ID")
  void shouldFetchCategoryAncestors() {
    var id = "ab9d66fc-f2cf-4357-b75a-09ff893f13f7";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    var entities = List.of(
        new Category(UUID.randomUUID().toString(), "Parent", null, null, null, null),
        new Category(UUID.randomUUID().toString(), "Grandparent", null, null, null, null)
    );
    when(categoryService.findCategoryAncestors(anyString(), anyInt(), any(ScrollPosition.class))).thenReturn(
        Window.from(entities, ignored -> ScrollPosition.keyset())
    );
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .variable("first", 5)
        .operationName("categoryWithAncestors")
        .execute()
        .path("category", category -> category.path("ancestors.edges[*].node").entityList(Category.class)
            .satisfies(ancestors -> assertThat(ancestors).isNotNull()
                .extracting(Category::title).contains("Parent", "Grandparent")))
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvYWI5ZDY2ZmMtZjJjZi00MzU3LWI3NWEtMDlmZjg5M2YxM2Y3"));
  }

  @Test
  @DisplayName("Should fetch category children by category ID")
  void shouldFetchCategoryChildren() {
    var id = "79d9cf75-0d30-4d1f-886b-7827deb98508";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    var entities = List.of(
        new Category(UUID.randomUUID().toString(), "Child", null, null, null, null),
        new Category(UUID.randomUUID().toString(), "Grandchild", null, null, null, null)
    );
    when(categoryService.findCategoryDescendants(anyString(), anyInt(), any(ScrollPosition.class))).thenReturn(
        Window.from(entities, ignored -> ScrollPosition.keyset())
    );
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .variable("first", 5)
        .operationName("categoryWithChildren")
        .execute()
        .path("category", category -> category.path("children.edges[*].node").entityList(Category.class)
            .satisfies(children -> assertThat(children).isNotNull()
                .extracting(Category::title).contains("Child", "Grandchild")))
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvNzlkOWNmNzUtMGQzMC00ZDFmLTg4NmItNzgyN2RlYjk4NTA4"));
  }

  @Test
  @DisplayName("Should check if category is root")
  void shouldFetchIfCategoryIsRoot() {
    var id = "79d9cf75-0d30-4d1f-886b-7827deb98508";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    when(categoryService.isRoot(anyString())).thenReturn(true);
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryWithIsRoot")
        .execute()
        .path("category", category -> category.path("isRoot").hasValue())
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvNzlkOWNmNzUtMGQzMC00ZDFmLTg4NmItNzgyN2RlYjk4NTA4"));
  }

  @Test
  @DisplayName("Should check if category is leaf")
  void shouldFetchIfCategoryIsLeaf() {
    var id = "79d9cf75-0d30-4d1f-886b-7827deb98508";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    when(categoryService.isLeaf(anyString())).thenReturn(true);
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryWithIsLeaf")
        .execute()
        .path("category", category -> category.path("isLeaf").hasValue())
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvNzlkOWNmNzUtMGQzMC00ZDFmLTg4NmItNzgyN2RlYjk4NTA4"));

  }

}
