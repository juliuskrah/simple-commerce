package com.simplecommerce.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * @author julius.krah
 */
interface Products extends Repository<ProductEntity, UUID> {
  boolean existsById(UUID id);

  @Query("SELECT p.tags FROM Product p WHERE p.id = :id")
  List<String> findTags(UUID id);

  Optional<ProductEntity> findById(UUID id);

  ProductEntity save(ProductEntity product);

  ProductEntity saveAndFlush(ProductEntity product);

  void deleteById(UUID id);
}
