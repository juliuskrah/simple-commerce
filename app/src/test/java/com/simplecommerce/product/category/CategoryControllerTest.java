package com.simplecommerce.product.category;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
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
    when(categoryService.findCategoryParents(anySet())).thenReturn(List.of(Optional.of(entity)));
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
    when(categoryService.findCategoryParents(anySet())).thenReturn(List.of(Optional.empty()));
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
    when(categoryService.findCategoryRootStatus(anySet())).thenReturn(List.of(true));
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
    when(categoryService.findCategoryLeafStatus(anySet())).thenReturn(List.of(true));
    graphQlTester.documentName("categoryDetails")
        .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
        .operationName("categoryWithIsLeaf")
        .execute()
        .path("category", category -> category.path("isLeaf").hasValue())
        .entity(Category.class).satisfies(category -> assertThat(category).isNotNull()
            .extracting(Category::id, as(InstanceOfAssertFactories.STRING)).isBase64()
            .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvNzlkOWNmNzUtMGQzMC00ZDFmLTg4NmItNzgyN2RlYjk4NTA4"));
  }

  @Test
  @DisplayName("Should create category")
  void shouldCreateCategory() {
    var inputMap = Map.of(
      "title", "Electronics",
      "slug", "electronics", 
      "description", "Electronic products"
    );
    var createdCategory = new Category("39a0ec70-de93-4005-8c73-d32f36d4ae55", "Electronics", "electronics", null, "Electronic products", null);
    
    when(categoryService.createCategory(any(CategoryInput.class))).thenReturn(createdCategory);
    
    graphQlTester.document("""
      mutation($input: CategoryInput!) {
        addCategory(input: $input) {
          id
          title
          slug
          description
        }
      }
      """)
      .variable("input", inputMap)
      .execute()
      .path("addCategory").entity(Category.class)
      .satisfies(category -> {
        assertThat(category).isNotNull();
        assertThat(category.title()).isEqualTo("Electronics");
        assertThat(category.slug()).isEqualTo("electronics");
        assertThat(category.description()).isEqualTo("Electronic products");
      });
    
    verify(categoryService).createCategory(any(CategoryInput.class));
  }

  @Test
  @DisplayName("Should update category")
  void shouldUpdateCategory() {
    var inputMap = Map.of(
      "title", "Updated Electronics",
      "slug", "updated-electronics",
      "description", "Updated electronic products"
    );
    var updatedCategory = new Category("39a0ec70-de93-4005-8c73-d32f36d4ae55", "Updated Electronics", "updated-electronics", null, "Updated electronic products", null);
    
    when(categoryService.updateCategory(anyString(), any(CategoryInput.class))).thenReturn(updatedCategory);
    
    graphQlTester.document("""
      mutation($id: ID!, $input: CategoryInput!) {
        updateCategory(id: $id, input: $input) {
          id
          title
          slug
          description
        }
      }
      """)
      .variable("id", "gid://SimpleCommerce/Category/39a0ec70-de93-4005-8c73-d32f36d4ae55")
      .variable("input", inputMap)
      .execute()
      .path("updateCategory").entity(Category.class)
      .satisfies(category -> {
        assertThat(category).isNotNull();
        assertThat(category.title()).isEqualTo("Updated Electronics");
        assertThat(category.slug()).isEqualTo("updated-electronics");
        assertThat(category.description()).isEqualTo("Updated electronic products");
      });
    
    verify(categoryService).updateCategory(anyString(), any(CategoryInput.class));
  }

  @Test
  @DisplayName("Should delete category")
  void shouldDeleteCategory() {
    var categoryId = "39a0ec70-de93-4005-8c73-d32f36d4ae55";
    
    when(categoryService.deleteCategory(anyString())).thenReturn(categoryId);
    
    graphQlTester.document("""
      mutation($id: ID!) {
        deleteCategory(id: $id)
      }
      """)
      .variable("id", "gid://SimpleCommerce/Category/39a0ec70-de93-4005-8c73-d32f36d4ae55")
      .execute()
      .path("deleteCategory").entity(String.class)
      .satisfies(id -> {
        assertThat(id).isNotNull();
        assertThat(id).isBase64();
      });
    
    verify(categoryService).deleteCategory(anyString());
  }

  @Test
  @DisplayName("Should fetch category breadcrumb")
  void shouldFetchCategoryBreadcrumb() {
    var id = "79d9cf75-0d30-4d1f-886b-7827deb98508";
    when(categoryService.findCategory(anyString())).thenReturn(new Category(id, null, null, null, null, null));
    when(categoryService.getCategoryBreadcrumbs(anySet())).thenReturn(List.of("Electronics > Computers > Laptops"));
    
    graphQlTester.document("""
      query($id: ID!) {
        category(id: $id) {
          id
          breadCrumb
        }
      }
      """)
      .variable("id", "gid://SimpleCommerce/Category/some-random-id-1234567")
      .execute()
      .path("category").entity(Category.class)
      .satisfies(category -> {
        assertThat(category).isNotNull();
        assertThat(category.id()).isNotNull();
      })
      .path("category.breadCrumb").entity(String.class)
      .isEqualTo("Electronics > Computers > Laptops");
    
    verify(categoryService).getCategoryBreadcrumbs(anySet());
  }

  @Test
  @DisplayName("Should update category with parent change (replanting)")
  void shouldUpdateCategoryWithParentChange() {
    var inputMap = Map.of(
      "title", "Mobile Phones",
      "slug", "mobile-phones",
      "description", "Mobile phone products",
      "parentId", "gid://SimpleCommerce/Category/parent-category-id"
    );
    var updatedCategory = new Category("39a0ec70-de93-4005-8c73-d32f36d4ae55", "Mobile Phones", "mobile-phones", null, "Mobile phone products", null);
    
    when(categoryService.updateCategory(anyString(), any(CategoryInput.class))).thenReturn(updatedCategory);
    
    graphQlTester.document("""
      mutation($id: ID!, $input: CategoryInput!) {
        updateCategory(id: $id, input: $input) {
          id
          title
          slug
          description
        }
      }
      """)
      .variable("id", "gid://SimpleCommerce/Category/39a0ec70-de93-4005-8c73-d32f36d4ae55")
      .variable("input", inputMap)
      .execute()
      .path("updateCategory").entity(Category.class)
      .satisfies(category -> {
        assertThat(category).isNotNull();
        assertThat(category.title()).isEqualTo("Mobile Phones");
        assertThat(category.slug()).isEqualTo("mobile-phones");
        assertThat(category.description()).isEqualTo("Mobile phone products");
      });
    
    verify(categoryService).updateCategory(anyString(), any(CategoryInput.class));
  }

}
