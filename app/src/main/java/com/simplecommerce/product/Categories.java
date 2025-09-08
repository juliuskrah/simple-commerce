package com.simplecommerce.product;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import java.util.Map;
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
import org.springframework.data.jpa.repository.Modifying;
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

  void deleteById(UUID id);

  /**
   * Check if a category slug already exists.
   * @param slug the slug to check
   * @return true if the slug exists
   */
  boolean existsBySlug(String slug);

  /**
   * Check if a category slug exists for a different category.
   * @param slug the slug to check
   * @param excludeId the ID to exclude from the check
   * @return true if the slug exists for a different category
   */
  @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.slug = :slug AND c.id != :excludeId")
  boolean existsBySlugAndIdNot(String slug, UUID excludeId);

  /**
   * Count the number of child categories.
   * @param id the parent category ID
   * @return the number of child categories
   */
  @Query("""
      SELECT COUNT(c)
      FROM Category c
      WHERE descendantsof(c.path, (SELECT path FROM Category WHERE id = :id))
        AND c.id != :id
      """)
  long countChildren(UUID id);

  /**
   * Update the paths of all descendants when moving a category branch.
   * This method updates all categories whose path starts with the old path prefix
   * by replacing it with the new path prefix.
   * @param oldPathPrefix the old path prefix to replace
   * @param newPathPrefix the new path prefix
   * @return the number of updated categories
   */
  @Modifying
  @Query("""
      UPDATE Category c 
      SET c.path = CONCAT(:newPathPrefix, SUBSTRING(c.path, LENGTH(:oldPathPrefix) + 1))
      WHERE c.path LIKE CONCAT(:oldPathPrefix, '%')
      """)
  int updateDescendantPaths(String oldPathPrefix, String newPathPrefix);

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

  // Batch query methods for performance optimization

  /**
   * Find multiple categories by their IDs in a single batch query.
   * @param ids the set of category IDs
   * @return list of category entities
   */
  @Query("SELECT c FROM Category c WHERE c.id IN :ids")
  List<CategoryEntity> findByIds(Set<UUID> ids);

  /**
   * Find parents of multiple categories in a single batch query.
   * @param ids the set of category IDs
   * @return map of child ID to parent entity
   */
  @Query("""
      SELECT c1.id, c2
      FROM Category c1, Category c2
      WHERE c1.id IN :ids
        AND sql('?::ltree', c2.path) = subpath(c1.path, 0, -1)
      """)
  List<Object[]> findParentsByIdsRaw(Set<UUID> ids);

  /**
   * Find leaf status of multiple categories in a single batch query.
   * @param ids the set of category IDs
   * @return map of category ID to leaf status
   */
  @Query("""
      SELECT c1.id, 
             NOT EXISTS (SELECT c2
                        FROM Category c2
                        WHERE c1.path <> c2.path
                          AND ancestorsof(c1.path, c2.path))
      FROM Category c1
      WHERE c1.id IN :ids
      """)
  List<Object[]> findLeafStatusByIdsRaw(Set<UUID> ids);

  /**
   * Find root status of multiple categories in a single batch query.
   * @param ids the set of category IDs
   * @return map of category ID to root status
   */
  @Query("""
      SELECT c1.id,
             NOT EXISTS(SELECT c2
                       FROM Category c2
                       WHERE sql('?::ltree', c2.path) = subpath(c1.path, 0, -1))
      FROM Category c1
      WHERE c1.id IN :ids
      """)
  List<Object[]> findRootStatusByIdsRaw(Set<UUID> ids);

  /**
   * Default method to convert raw parent query results to a map.
   */
  default Map<UUID, Optional<CategoryEntity>> findParentsByIds(Set<UUID> ids) {
    var results = findParentsByIdsRaw(ids);
    var parentMap = new java.util.HashMap<UUID, Optional<CategoryEntity>>();
    
    // Initialize all IDs with empty optional
    for (UUID id : ids) {
      parentMap.put(id, Optional.empty());
    }
    
    // Fill in the parents that exist
    for (Object[] result : results) {
      UUID childId = (UUID) result[0];
      CategoryEntity parent = (CategoryEntity) result[1];
      parentMap.put(childId, Optional.of(parent));
    }
    
    return parentMap;
  }

  /**
   * Default method to convert raw leaf status query results to a map.
   */
  default Map<UUID, Boolean> findLeafStatusByIds(Set<UUID> ids) {
    var results = findLeafStatusByIdsRaw(ids);
    var statusMap = new java.util.HashMap<UUID, Boolean>();
    
    for (Object[] result : results) {
      UUID categoryId = (UUID) result[0];
      Boolean isLeaf = (Boolean) result[1];
      statusMap.put(categoryId, isLeaf);
    }
    
    return statusMap;
  }

  /**
   * Default method to convert raw root status query results to a map.
   */
  default Map<UUID, Boolean> findRootStatusByIds(Set<UUID> ids) {
    var results = findRootStatusByIdsRaw(ids);
    var statusMap = new java.util.HashMap<UUID, Boolean>();
    
    for (Object[] result : results) {
      UUID categoryId = (UUID) result[0];
      Boolean isRoot = (Boolean) result[1];
      statusMap.put(categoryId, isRoot);
    }
    
    return statusMap;
  }
}
