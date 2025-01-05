package com.simplecommerce.product;

import com.simplecommerce.node.NodeService;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
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
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Category> findCategoryParent(String id) {
    return Optional.empty();
  }

  @Override
  public Window<Category> findCategoryAncestors(String id, int limit, ScrollPosition scroll) {
    return null;
  }

  @Override
  public Window<Category> findCategoryDescendants(String id, int limit, ScrollPosition scroll) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<Integer> findCategoryLevels(Set<String> ids) {
    return Stream.empty();
  }

  @Override
  public boolean isLeaf(String id) {
    return false;
  }

  @Override
  public boolean isRoot(String id) {
    return false;
  }
}
