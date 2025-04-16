package com.simplecommerce.product;

import static com.simplecommerce.shared.VirtualThreadHelper.callInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    return new Category(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getSlug(),
        entity.getCreatedAt(),
        entity.getDescription(),
        entity.getUpdatedAt()
    );
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  private List<Integer> levelsWithinTransaction(Set<UUID> ids) {
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
  public boolean isRoot(String id) {
    return callInScope(() -> categoryRepository.isRoot(UUID.fromString(id)));
  }
}
