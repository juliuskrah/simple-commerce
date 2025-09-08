package com.simplecommerce.product;

import static com.simplecommerce.shared.VirtualThreadHelper.callInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import com.simplecommerce.shared.Slug;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author julius.krah
 */
@Transactional(readOnly = true)
@Configurable(autowire = Autowire.BY_TYPE)
class CategoryManagement implements CategoryService, NodeService {

  public void setCategoryRepository(ObjectFactory<Categories> categoryRepository) {
    this.categoryRepository = categoryRepository.getObject();
  }

  private Categories categoryRepository;

  private Category fromEntity(CategoryEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new Category(
        entity.getId() != null ? entity.getId().toString() : "",
        entity.getTitle(),
        entity.getSlug(),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getDescription(),
        entity.getLastModifiedDate().orElseGet(epoch)
    );
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  List<Integer> levelsWithinTransaction(Set<UUID> ids) {
    try(var stream = categoryRepository.findTreeLevel(ids)) {
      return stream.toList();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Category node(String id) {
    return findCategory(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Category findCategory(String id) {
    var gid = GlobalId.decode(id);
    var category = callInScope(() -> categoryRepository.findById(UUID.fromString(gid.id())));
    return category.map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Window<Category> findCategories(int limit, ScrollPosition scroll) {
    return callInScope(() -> categoryRepository.findBy(Limit.of(limit), Sort.by("title"), scroll))
        .map(this::fromEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Category> findProductCategory(String productId) {
    return callInScope(() -> categoryRepository.findOneByProductId(UUID.fromString(productId))
        .map(this::fromEntity));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Category> findCategoryParent(String id) {
    return callInScope(() -> categoryRepository.findParent(UUID.fromString(id))
        .map(this::fromEntity));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Optional<Category>> findCategoryParents(Set<String> ids) {
    var categoryIds = ids.stream().map(UUID::fromString).collect(Collectors.toSet());
    return callInScope(() -> {
      var parentMap = categoryRepository.findParentsByIds(categoryIds);
      return ids.stream()
          .map(id -> parentMap.get(UUID.fromString(id)).map(this::fromEntity))
          .collect(Collectors.toList());
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Window<Category> findCategoryAncestors(String id, int limit, ScrollPosition scroll) {
    return callInScope(() -> categoryRepository.findAncestorsById(
        UUID.fromString(id), Limit.of(limit), Sort.unsorted(), scroll)
        .map(this::fromEntity));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Window<Category> findCategoryDescendants(String id, int limit, ScrollPosition scroll) {
    return callInScope(() -> categoryRepository.findDescendantsById(
        UUID.fromString(id), Limit.of(limit), Sort.unsorted(), scroll)
        .map(this::fromEntity));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Integer> findCategoryLevels(Set<String> ids) {
    var categoryIds = ids.stream().map(UUID::fromString).collect(Collectors.toSet());
    return levelsWithinTransaction(categoryIds);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLeaf(String id) {
    return callInScope(() -> categoryRepository.isLeaf(UUID.fromString(id)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Boolean> findCategoryLeafStatus(Set<String> ids) {
    var categoryIds = ids.stream().map(UUID::fromString).collect(Collectors.toSet());
    return callInScope(() -> {
      var leafStatusMap = categoryRepository.findLeafStatusByIds(categoryIds);
      return ids.stream()
          .map(id -> leafStatusMap.getOrDefault(UUID.fromString(id), false))
          .collect(Collectors.toList());
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRoot(String id) {
    return callInScope(() -> categoryRepository.isRoot(UUID.fromString(id)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Boolean> findCategoryRootStatus(Set<String> ids) {
    var categoryIds = ids.stream().map(UUID::fromString).collect(Collectors.toSet());
    return callInScope(() -> {
      var rootStatusMap = categoryRepository.findRootStatusByIds(categoryIds);
      return ids.stream()
          .map(id -> rootStatusMap.getOrDefault(UUID.fromString(id), false))
          .collect(Collectors.toList());
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public Category createCategory(CategoryInput input) {
    return callInScope(() -> {
      var entity = new CategoryEntity();
      entity.setTitle(input.title());
      entity.setDescription(input.description());
      
      // Generate slug if not provided
      var slug = input.slug() != null ? input.slug() : Slug.generate(input.title());
      
      // Check for slug uniqueness
      if (categoryRepository.existsBySlug(slug)) {
        throw new IllegalArgumentException("Category with slug '" + slug + "' already exists");
      }
      entity.setSlug(slug);
      
      // Handle parent and path
      String path;
      if (input.parentId() != null) {
        var parentGid = GlobalId.decode(input.parentId());
        var parentId = UUID.fromString(parentGid.id());
        var parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new NotFoundException("Parent category not found"));
        
        // Create path based on parent
        path = parent.getPath() + "." + slug;
      } else {
        // Root category
        path = slug;
      }
      entity.setPath(path);
      
      var saved = categoryRepository.saveAndFlush(entity);
      return fromEntity(saved);
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public Category updateCategory(String id, CategoryInput input) {
    return callInScope(() -> {
      var gid = GlobalId.decode(id);
      var categoryId = UUID.fromString(gid.id());
      var entity = categoryRepository.findById(categoryId)
          .orElseThrow(NotFoundException::new);
      
      // Update basic fields
      entity.setTitle(input.title());
      entity.setDescription(input.description());
      
      // Handle slug update
      var newSlug = input.slug() != null ? input.slug() : Slug.generate(input.title());
      boolean slugChanged = !entity.getSlug().equals(newSlug);
      if (slugChanged) {
        // Check slug uniqueness
        if (categoryRepository.existsBySlugAndIdNot(newSlug, categoryId)) {
          throw new IllegalArgumentException("Category with slug '" + newSlug + "' already exists");
        }
        entity.setSlug(newSlug);
      }
      
      // Handle parent change (replanting)
      boolean parentChanged = false;
      String oldPath = entity.getPath();
      String newPath = oldPath;
      
      if (input.parentId() != null) {
        var newParentGid = GlobalId.decode(input.parentId());
        var newParentId = UUID.fromString(newParentGid.id());
        
        // Validate that we're not creating a cycle
        if (categoryId.equals(newParentId)) {
          throw new IllegalArgumentException("Category cannot be its own parent");
        }
        
        // Check if the new parent is a descendant of the current category (would create cycle)
        var newParent = categoryRepository.findById(newParentId)
            .orElseThrow(() -> new NotFoundException("Parent category not found"));
        
        if (newParent.getPath().startsWith(oldPath + ".")) {
          throw new IllegalArgumentException("Cannot move category under its own descendant");
        }
        
        // Calculate new path
        newPath = newParent.getPath() + "." + newSlug;
        parentChanged = !oldPath.equals(newPath);
        
      } else {
        // Moving to root level
        newPath = newSlug;
        parentChanged = !oldPath.equals(newPath);
      }
      
      // Update paths if needed
      if (slugChanged || parentChanged) {
        // First, update all descendant paths if this category is being moved or renamed
        if (parentChanged || slugChanged) {
          categoryRepository.updateDescendantPaths(oldPath, newPath);
        }
        
        // Update the current category's path
        entity.setPath(newPath);
      }
      
      var saved = categoryRepository.saveAndFlush(entity);
      return fromEntity(saved);
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public String deleteCategory(String id) {
    return callInScope(() -> {
      var gid = GlobalId.decode(id);
      var categoryId = UUID.fromString(gid.id());
      
      // Check if category exists
      if (!categoryRepository.findById(categoryId).isPresent()) {
        throw new NotFoundException("Category not found");
      }
      
      // Check if category has children
      if (categoryRepository.countChildren(categoryId) > 0) {
        throw new IllegalArgumentException("Cannot delete category with child categories");
      }
      
      categoryRepository.deleteById(categoryId);
      return gid.id();
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCategoryBreadcrumb(String id) {
    return callInScope(() -> {
      var categoryId = UUID.fromString(id);
      var category = categoryRepository.findById(categoryId)
          .orElseThrow(NotFoundException::new);
      
      // Get all ancestors including the category itself
      try (var ancestorStream = categoryRepository.findAncestorsById(categoryId)) {
        var ancestors = ancestorStream.toList();
        
        // Add the current category to the list
        var allCategories = new java.util.ArrayList<>(ancestors);
        allCategories.add(category);
        
        // Sort by path length to get correct order (root to leaf)
        allCategories.sort((a, b) -> {
          int aLevel = a.getPath().split("\\.").length;
          int bLevel = b.getPath().split("\\.").length;
          return Integer.compare(aLevel, bLevel);
        });
        
        // Build breadcrumb string
        return allCategories.stream()
            .map(CategoryEntity::getTitle)
            .collect(java.util.stream.Collectors.joining(" > "));
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getCategoryBreadcrumbs(Set<String> ids) {
    var categoryIds = ids.stream().map(UUID::fromString).collect(Collectors.toSet());
    return callInScope(() -> {
      // Get all requested categories
      var categoriesMap = categoryRepository.findByIds(categoryIds).stream()
          .collect(Collectors.toMap(CategoryEntity::getId, entity -> entity));
      
      // Get all unique path segments needed for breadcrumbs
      var allPathSegments = new java.util.HashSet<String>();
      for (var category : categoriesMap.values()) {
        var pathParts = category.getPath().split("\\.");
        var currentPath = new StringBuilder();
        for (int i = 0; i < pathParts.length; i++) {
          if (i > 0) currentPath.append(".");
          currentPath.append(pathParts[i]);
          allPathSegments.add(currentPath.toString());
        }
      }
      
      // Get all categories for the path segments in one query
      var allCategoriesForPaths = categoryRepository.findAll(
          (root, query, builder) -> root.get("path").in(allPathSegments)
      ).stream().collect(Collectors.toMap(CategoryEntity::getPath, entity -> entity));
      
      return ids.stream()
          .map(id -> {
            var categoryId = UUID.fromString(id);
            var category = categoriesMap.get(categoryId);
            if (category == null) return "";
            
            // Build breadcrumb from path segments
            var pathParts = category.getPath().split("\\.");
            var titles = new java.util.ArrayList<String>();
            var currentPath = new StringBuilder();
            
            for (int i = 0; i < pathParts.length; i++) {
              if (i > 0) currentPath.append(".");
              currentPath.append(pathParts[i]);
              
              var pathCategory = allCategoriesForPaths.get(currentPath.toString());
              if (pathCategory != null) {
                titles.add(pathCategory.getTitle());
              }
            }
            
            return String.join(" > ", titles);
          })
          .collect(Collectors.toList());
    });
  }
}
