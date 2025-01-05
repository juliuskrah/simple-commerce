package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

/**
 * @author julius.krah
 */
@ExtendWith(MockitoExtension.class)
class CategoryManagementTest {
  @Mock
  private Categories categoryRepository;
  @InjectMocks
  CategoryManagement categoryService;

  @Test
  void shouldResolveNode() {
    var entity = new CategoryEntity();
    entity.setId(UUID.fromString("1c7e429c-45ed-45d7-9d1a-36f561b9d6b9"));
    entity.setTitle("Electronics");
    when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/Category/1c7e429c-45ed-45d7-9d1a-36f561b9d6b9
    var category = categoryService.node("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvMWM3ZTQyOWMtNDVlZC00NWQ3LTlkMS0zNmY1NjFiOWQ2Yjk=");
    assertThat(category).isNotNull()
        .hasFieldOrPropertyWithValue("id", "1c7e429c-45ed-45d7-9d1a-36f561b9d6b9")
        .hasFieldOrPropertyWithValue("title", "Electronics");
  }

  @Test
  void shouldFindCategory() {
    var entity = new CategoryEntity();
    entity.setId(UUID.fromString("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c"));
    entity.setTitle("Computers");
    when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/Category/7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c
    var category = categoryService.findCategory("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvNzAwNGViYmMtZTcxYy00NWYzLThkMjMtMWJhMmMzN2YyZjFj");
    assertThat(category).isNotNull()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Computers");
  }

  @Test
  void shouldNotFindCategory() {
    when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
    // gid://SimpleCommerce/Category/7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c
    var throwable = catchThrowable(() -> categoryService.findCategory("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvQ2F0ZWdvcnkvNzAwNGViYmMtZTcxYy00NWYzLThkMjMtMWJhMmMzN2YyZjFj"));
    assertThat(throwable).isInstanceOf(NotFoundException.class);
  }

  @Test
  void shouldFindCategoryParent() {
    var entity = new CategoryEntity();
    entity.setId(UUID.fromString("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c"));
    entity.setTitle("Computers");
    when(categoryRepository.findParent(any(UUID.class))).thenReturn(Optional.of(entity));
    var category = categoryService.findCategoryParent(UUID.randomUUID().toString());
    assertThat(category).isPresent().get()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Computers");
  }

  @Test
  void shouldFindCategoryAncestors() {
    var entity = new CategoryEntity();
    entity.setId(UUID.fromString("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c"));
    entity.setTitle("Computers");
    when(categoryRepository.findAncestorsById(any(UUID.class), any(Limit.class), any(Sort.class), any(ScrollPosition.class)))
        .thenReturn(Window.from(List.of(entity), ignored -> ScrollPosition.keyset()));
    var category = categoryService.findCategoryAncestors(UUID.randomUUID().toString(), 1, ScrollPosition.keyset());
    assertThat(category).isNotNull()
        .hasSize(1).first()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Computers");
  }

  @Test
  void shouldFindCategoryDescendants() {
    var entity = new CategoryEntity();
    entity.setId(UUID.fromString("7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c"));
    entity.setTitle("Computers");
    when(categoryRepository.findDescendantsById(any(UUID.class), any(Limit.class), any(Sort.class), any(ScrollPosition.class)))
        .thenReturn(Window.from(List.of(entity), ignored -> ScrollPosition.keyset()));
    var category = categoryService.findCategoryDescendants(UUID.randomUUID().toString(), 1, ScrollPosition.keyset());
    assertThat(category).isNotNull()
        .hasSize(1).first()
        .hasFieldOrPropertyWithValue("id", "7004ebbc-e71c-45f3-8d23-1ba2c37f2f1c")
        .hasFieldOrPropertyWithValue("title", "Computers");
  }

  @Test
  void shouldFindCategoryLevels() {
    when(categoryRepository.findTreeLevel(anySet())).thenReturn(Stream.of(1));
    var category = categoryService.findCategoryLevels(Set.of(UUID.randomUUID().toString()));
    assertThat(category).isNotEmpty().hasSize(1).contains(1);
  }

  @Test
  void shouldFindCategoryIsLeaf() {
    when(categoryRepository.isLeaf(any())).thenReturn(true);
    var category = categoryService.isLeaf(UUID.randomUUID().toString());
    assertThat(category).isTrue();
  }

  @Test
  void shouldFindCategoryIsRoot() {
    when(categoryRepository.isRoot(any())).thenReturn(true);
    var category = categoryService.isRoot(UUID.randomUUID().toString());
    assertThat(category).isTrue();
  }
}