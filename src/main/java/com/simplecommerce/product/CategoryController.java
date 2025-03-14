package com.simplecommerce.product;

import static com.simplecommerce.shared.Types.NODE_CATEGORY;
import static java.util.stream.Collectors.toSet;

import com.simplecommerce.shared.Actor;
import com.simplecommerce.shared.GlobalId;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * @author julius.krah
 */
@Controller
class CategoryController {

  private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);
  private final ObjectProvider<CategoryService> categoryService;
  // Defer creation of the CategoryService to avoid early initialization of aspectj proxy
  private final Supplier<CategoryService> categoryServiceSupplier = SingletonSupplier.of(CategoryManagement::new);

  CategoryController(ObjectProvider<CategoryService> categoryService) {
    this.categoryService = categoryService;
  }

  @QueryMapping
  Category category(@Argument String id) {
    return categoryService.getIfAvailable(categoryServiceSupplier).findCategory(id);
  }

  @SchemaMapping
  String id(Category source) {
    return new GlobalId(NODE_CATEGORY, source.id()).encode();
  }

  @SchemaMapping
  Optional<Category> parent(Category source) {
    return categoryService.getIfAvailable(categoryServiceSupplier).findCategoryParent(source.id());
  }

  @SchemaMapping
  Optional<Category> category(Product source) {
    return categoryService.getIfAvailable(categoryServiceSupplier).findProductCategory(source.id());
  }

  @SchemaMapping(typeName = "Category")
  Optional<Actor> createdBy() {
    return Optional.empty();
  }

  @SchemaMapping(typeName = "Category")
  Optional<Actor> updatedBy() {
    return Optional.empty();
  }

  @SchemaMapping
  boolean isLeaf(Category source) {
    return categoryService.getIfAvailable(categoryServiceSupplier).isLeaf(source.id());
  }

  @SchemaMapping
  boolean isRoot(Category source) {
    return categoryService.getIfAvailable(categoryServiceSupplier).isRoot(source.id());
  }

  @SchemaMapping
  Window<Category> ancestors(Category source, ScrollSubrange subrange) {
    var limit = subrange.count().orElse(100);
    var scroll = subrange.position().orElse(ScrollPosition.keyset());
    return categoryService.getIfAvailable(categoryServiceSupplier).findCategoryAncestors(source.id(), limit, scroll);
  }

  @SchemaMapping
  Window<Category> children(Category source, ScrollSubrange subrange) {
    var limit = subrange.count().orElse(100);
    var scroll = subrange.position().orElse(ScrollPosition.keyset());
    return categoryService.getIfAvailable(categoryServiceSupplier).findCategoryDescendants(source.id(), limit, scroll);
  }

  @BatchMapping
  List<Integer> level(List<Category> categories) {
    var categoryIds = categories.stream().map(Category::id).collect(toSet());
    LOG.debug("Fetching category-level for {} categories: {}", categories.size(), categoryIds);
    return categoryService.getIfAvailable(categoryServiceSupplier).findCategoryLevels(categoryIds);
  }
}
