package com.simplecommerce.product;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * @author julius.krah
 */
interface Products extends Repository<ProductEntity, UUID> {

  @Query("SELECT p.tags FROM Product p WHERE p.id = :id")
  List<String> findTags(UUID id, Limit limit);

  /**
   * Find tags for a list of products.
   * @param limitTags The number of tags to return.
   * @param ids The list of product IDs.
   * @return A list of products with tags.
   */
  @Query(value = """
    SELECT DISTINCT product_id AS id,
    (array_agg(tags))[:?1] AS tags
    FROM product_tag WHERE product_id IN ?2 GROUP BY product_id
    """, nativeQuery = true)
  List<ProductWithTags> findTags(int limitTags, Set<UUID> ids);

  Optional<ProductEntity> findById(UUID id);

  @Query("SELECT p FROM Product p")
  List<ProductEntity> findBy(Limit limit);

  Window<ProductEntity> findBy(Limit limit, ScrollPosition scroll);

  ProductEntity saveAndFlush(ProductEntity product);

  void deleteById(UUID id);
}
