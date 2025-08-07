package com.simplecommerce.product;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * @author julius.krah
 */
interface Categories extends Repository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {

  /**
   * {@code SELECT EXISTS (SELECT *
   *                FROM categories AS c1
   *                WHERE NOT EXISTS (SELECT 1
   *                                  FROM categories AS c2
   *                                  WHERE c1.path <> c2.path
   *                                    AND c1.path @> c2.path)
   *                  AND c1.id = ?) AS is_leaf}
   * @param id the id of the category
   * @return true if the category is a leaf node
   */
  @Query("""
      SELECT EXISTS (SELECT c1
               FROM Category c1
               WHERE NOT EXISTS (SELECT c2
                                 FROM Category c2
                                 WHERE c1.path <> c2.path
                                   AND ancestorsof(c1.path, c2.path))
                 AND c1.id = :id)
      """)
  boolean isLeaf(UUID id);

  /**
   * {@code SELECT NOT EXISTS(SELECT *
   *                   FROM categories
   *                   WHERE path = subpath((SELECT path FROM categories WHERE id = ?), '0', '-1')) AS is_root}
   * @param id the id of the category
   * @return true if the category is a root node
   */
  @Query("""
      SELECT NOT EXISTS(SELECT c
                  FROM Category c
                  WHERE sql('?::ltree', c.path) = subpath((SELECT path FROM Category WHERE id = :id), 0, -1))
      """)
  boolean isRoot(UUID id);

  CategoryEntity saveAndFlush(CategoryEntity category);

  Optional<CategoryEntity> findById(UUID id);

  /**
   * Get the category of a product ID.
   * @param productId the product ID
   * @return the category of the product
   */
  @Query("""
      SELECT c
      FROM Category c JOIN Product p ON c.id = p.category.id
      WHERE p.id = :productId
      """)
  Optional<CategoryEntity> findOneByProductId(UUID productId);

  @Query("SELECT nlevel(path) AS level FROM Category WHERE id = :id")
  Optional<Integer> findTreeLevel(UUID id);

  /**
   * {@code SELECT *
   * FROM categories
   * WHERE path = subpath((SELECT path FROM categories WHERE id = :id), 0, -1)}
   * @return the parent category if it exists.
   */
  @Query("""
      SELECT c
      FROM Category c
      WHERE sql('?::ltree', c.path) = subpath((SELECT path FROM Category WHERE id = :id), 0, -1)
      """)
  Optional<CategoryEntity> findParent(UUID id);

  @Query("SELECT nlevel(path) AS level FROM Category WHERE id in :ids")
  Stream<Integer> findTreeLevel(Set<UUID> ids);

  /**
   * {@code select *
   * from categories
   * where (path @> (select path
   *                from categories
   *                where id = :id))}
   * @see com.simplecommerce.shared.hql.AncestorsOfSQLFunction
   * @param id the id of the category
   * @return ancestors of the category with the given id
   */
  @Query("""
      SELECT c
      FROM Category c
      WHERE ancestorsof(c.path, (SELECT path
                     FROM Category
                     WHERE id = :id))
      """)
  Stream<CategoryEntity> findAncestorsById(UUID id);

  /**
   * {@code select *
   * from categories
   * where (path <@ (select path
   *                from categories
   *                where id = :id))}
   * @see com.simplecommerce.shared.hql.DescendantsOfSQLFunction
   * @param id the id of the category
   * @return descendants of the category with the given id
   */
  @Query("""
      SELECT c
      FROM Category c
      WHERE descendantsof(c.path, (SELECT path
                     FROM Category
                     WHERE id = :id))
      """)
  Stream<CategoryEntity> findDescendantsById(UUID id);

  Window<CategoryEntity> findBy(Limit limit, Sort sort, ScrollPosition position);

  default Window<CategoryEntity> findAncestorsById(UUID id, Limit limit, Sort sort, ScrollPosition position) {
    return findBy(ancestorsForId(id), function -> {
      if (limit.isLimited()) {
        return function.limit(limit.max()).sortBy(sort).scroll(position);
      }
      return function.sortBy(sort).scroll(position);
    });
  }


  default Window<CategoryEntity> findDescendantsById(UUID id, Limit limit, Sort sort, ScrollPosition position) {
    return findBy(descendantsForId(id), function -> {
      if (limit.isLimited()) {
        return function.limit(limit.max()).sortBy(sort).scroll(position);
      }
      return function.sortBy(sort).scroll(position);
    });
  }

  private Specification<CategoryEntity> ancestorsForId(UUID id) {
    return (root, query, builder) -> {
      Subquery<String> subquery = query.subquery(String.class);
      Root<CategoryEntity> from = subquery.from(CategoryEntity.class);
      return builder.isTrue(
            builder.function("ancestorsof", Boolean.class, root.get("path"),
                subquery.select(from.get("path")).where(builder.equal(from.get("id"), id))));
    };
  }

  private Specification<CategoryEntity> descendantsForId(UUID id) {
    return (root, query, builder) -> {
      Subquery<String> subquery = query.subquery(String.class);
      Root<CategoryEntity> from = subquery.from(CategoryEntity.class);
      return builder.isTrue(
          builder.function("descendantsof", Boolean.class, root.get("path"),
              subquery.select(from.get("path")).where(builder.equal(from.get("id"), id))));
    };
  }
}
