package com.simplecommerce.product;

import static org.springframework.data.jpa.domain.Specification.where;

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
  CategoryEntity saveAndFlush(CategoryEntity category);

  Optional<CategoryEntity> findById(UUID id);

  @Query("SELECT nlevel(path) AS level FROM Category WHERE id = :id")
  Optional<Integer> findTreeLevel(UUID id);

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

  default Window<CategoryEntity> findAncestorsById(UUID id, Limit limit, Sort sort, ScrollPosition position) {
    return findBy(where(ancestorsForId(id)), function -> {
      if (limit.isLimited()) {
        return function.limit(limit.max()).sortBy(sort).scroll(position);
      }
      return function.sortBy(sort).scroll(position);
    });
  }

  Window<CategoryEntity> findBy(Limit limit, Sort sort, ScrollPosition position);

  private Specification<CategoryEntity> ancestorsForId(UUID id) {
    return (root, query, builder) -> {
      Subquery<String> subquery = query.subquery(String.class);
      Root<CategoryEntity> from = subquery.from(CategoryEntity.class);
      return builder.isTrue(
            builder.function("ancestorsof", Boolean.class, root.get("path"),
                subquery.select(from.get("path")).where(builder.equal(from.get("id"), id))));
    };
  }
}
