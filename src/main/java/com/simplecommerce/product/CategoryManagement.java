package com.simplecommerce.product;

import com.simplecommerce.node.NodeService;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author julius.krah
 */
@Transactional(readOnly = true)
@Configurable(autowire = Autowire.BY_TYPE)
class CategoryManagement implements CategoryService, NodeService {

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
  public Stream<Integer> findCategoryLevels(Set<String> ids) {
    return Stream.empty();
  }
}
