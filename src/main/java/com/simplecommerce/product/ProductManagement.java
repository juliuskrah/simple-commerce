package com.simplecommerce.product;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import com.simplecommerce.shared.Slug;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
class ProductManagement implements ProductService, NodeService {
  private final ReentrantLock lock = new ReentrantLock();
  @Autowired
  private Products productRepository;

  void runInScope(Runnable run) {
    lock.lock();
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var task = executor.submit(run);
      task.get();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  <R> R callInScope(Callable<R> call) {
    lock.lock();
    try (var scope = new ShutdownOnFailure()) {
      var task = scope.fork(call);
      scope.join().throwIfFailed();
      return task.get();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  private ProductEntity toEntity(ProductInput product) {
    var entity = new ProductEntity();
    entity.setTitle(product.title());
    entity.setSlug(Slug.generate(product.title()));
    entity.setDescription(product.description());
    entity.setTags(product.tags());
    return entity;
  }

  private Product fromEntity(ProductEntity entity) {
    return new Product(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getSlug(),
        entity.getCreatedAt(),
        entity.getDescription(),
        entity.getUpdatedAt()
    );
  }

  @Transactional(propagation = Propagation.NESTED)
  private Optional<ProductEntity> updateProduct(ProductInput product, String productId) {
    var gid = GlobalId.decode(productId);
    return productRepository.findById(UUID.fromString(gid.id()))
        .map(productEntity -> {
          productEntity.setTitle(product.title());
          productEntity.setDescription(product.description());
          productEntity.setTags(product.tags());
          return productEntity;
        });
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public Product node(String id) {
    return findProduct(id);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public Product findProduct(String id) {
    var gid = GlobalId.decode(id);
    var product = callInScope(() -> productRepository.findById(UUID.fromString(gid.id())));
    return product.map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> findTags(String productId, int limit) {
    return callInScope(() -> productRepository.findTags(UUID.fromString(productId), Limit.of(limit)));
  }

  @Override
  public List<ProductWithTags> findTags(Set<String> productIds, int limit) {
    var ids = productIds.stream().map(UUID::fromString).collect(Collectors.toSet());
    return callInScope(() -> productRepository.findTags(limit, ids));
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public List<Product> findProducts(int limit) {
    return productRepository.findBy(Limit.of(limit)).stream().map(this::fromEntity).toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String deleteProduct(String id) {
    var gid = GlobalId.decode(id);
    runInScope(() -> productRepository.deleteById(UUID.fromString(gid.id())));
    return gid.id();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Product createProduct(ProductInput product) {
    var productEntity = toEntity(product);
    productEntity.publishProductCreatedEvent();
    runInScope(() -> productRepository.saveAndFlush(productEntity));
    return fromEntity(productEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Product updateProduct(String productId, ProductInput product) {
    return callInScope(() -> updateProduct(product, productId))
        .map(this::fromEntity)
        .orElseThrow(NotFoundException::new);
  }
}
