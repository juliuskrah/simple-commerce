package com.simplecommerce.product.search;

import com.simplecommerce.product.ProductEntity;
import com.simplecommerce.shared.types.ProductStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Translates parsed search queries into JPA Specifications for database queries.
 * Supported fields:
 * - status: product status (draft, published, archived)
 * - title: product title (text search)
 * - price: product price (comparison and range)
 * - created: creation date (date range)
 * - updated: update date (date range)
 * - category: category name
 * - tags: product tags
 * 
 * @author julius.krah
 * @since 1.0
 */
@Service
public class SearchQueryTranslator {

  private static final Logger LOG = LoggerFactory.getLogger(SearchQueryTranslator.class);

  /**
   * Translates a SearchQuery into a JPA Specification for ProductEntity.
   * 
   * @param searchQuery the parsed search query
   * @return JPA Specification for database filtering
   */
  public Specification<ProductEntity> translateToSpecification(SearchQuery searchQuery) {
    LOG.debug("Translating search query to specification: {}", searchQuery);

    if (searchQuery.isEmpty()) {
      return Specification.unrestricted(); // No filtering
    }

    return (root, query, criteriaBuilder) -> {
      var predicates = new ArrayList<Predicate>();

      for (SearchTerm term : searchQuery.terms()) {
        var predicate = translateTerm(term, root, query, criteriaBuilder);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }

      if (predicates.isEmpty()) {
        return criteriaBuilder.conjunction(); // Always true
      }

      // Combine predicates based on the query operator
      return switch (searchQuery.operator()) {
        case AND -> criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        case OR -> criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        case NOT -> criteriaBuilder.not(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
      };
    };
  }

  /**
   * Translates a single search term into a JPA Predicate.
   */
  private Predicate translateTerm(SearchTerm term, 
                                 Root<ProductEntity> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder criteriaBuilder) {
    
    LOG.debug("Translating search term: {}", term);

    return switch (term.field().toLowerCase()) {
      case "status" -> translateStatusTerm(term, root, criteriaBuilder);
      case "title" -> translateTextTerm(term, root.get("title"), criteriaBuilder);
      case "description" -> translateTextTerm(term, root.get("description"), criteriaBuilder);
      case "slug" -> translateTextTerm(term, root.get("slug"), criteriaBuilder);
      case "price" -> translatePriceTerm(term, root, criteriaBuilder);
      case "created" -> translateDateTerm(term, root.get("createdAt"), criteriaBuilder);
      case "updated" -> translateDateTerm(term, root.get("updatedAt"), criteriaBuilder);
      case "category" -> translateCategoryTerm(term, root, criteriaBuilder);
      case "tags" -> translateTagsTerm(term, root, criteriaBuilder);
      case "text" -> translateFullTextSearch(term, root, criteriaBuilder);
      default -> {
        LOG.warn("Unknown search field: {}", term.field());
        yield null; // Ignore unknown fields
      }
    };
  }

  /**
   * Translates status-related search terms.
   */
  private Predicate translateStatusTerm(SearchTerm term, 
                                       Root<ProductEntity> root,
                                       CriteriaBuilder criteriaBuilder) {
    try {
      var status = ProductStatus.valueOf(term.value().toUpperCase());
      return switch (term.operator()) {
        case EQUALS -> criteriaBuilder.equal(root.get("status"), status);
        case NOT_EQUALS -> criteriaBuilder.notEqual(root.get("status"), status);
        default -> {
          LOG.warn("Unsupported operator {} for status field", term.operator());
          yield null;
        }
      };
    } catch (IllegalArgumentException e) {
      LOG.warn("Invalid status value: {}", term.value());
      return null;
    }
  }

  /**
   * Translates text-based search terms.
   */
  private Predicate translateTextTerm(SearchTerm term, 
                                     Path<String> path,
                                     CriteriaBuilder criteriaBuilder) {
    return switch (term.operator()) {
      case EQUALS -> criteriaBuilder.equal(path, term.value());
      case NOT_EQUALS -> criteriaBuilder.notEqual(path, term.value());
      case CONTAINS -> criteriaBuilder.like(
          criteriaBuilder.lower(path), 
          "%" + term.value().toLowerCase() + "%"
      );
      case STARTS_WITH -> criteriaBuilder.like(
          criteriaBuilder.lower(path), 
          term.value().toLowerCase() + "%"
      );
      case ENDS_WITH -> criteriaBuilder.like(
          criteriaBuilder.lower(path), 
          "%" + term.value().toLowerCase()
      );
      default -> {
        LOG.warn("Unsupported operator {} for text field", term.operator());
        yield null;
      }
    };
  }

  /**
   * Translates price-related search terms.
   * Note: This is a simplified implementation. In a real scenario,
   * you'd need to join with variants and consider contextual pricing.
   */
  private Predicate translatePriceTerm(SearchTerm term, 
                                      Root<ProductEntity> root,
                                      CriteriaBuilder criteriaBuilder) {
    try {
      if (term.isRange()) {
        var startValue = new BigDecimal(term.getRangeStart());
        var endValue = new BigDecimal(term.getRangeEnd());
        
        // This would need to be enhanced to join with variants and price sets
        LOG.warn("Price range queries not fully implemented yet: {}", term);
        return null;
      } else {
        var value = new BigDecimal(term.value());
        // Simplified implementation - would need proper variant joining
        LOG.warn("Price comparison queries not fully implemented yet: {}", term);
        return null;
      }
    } catch (NumberFormatException e) {
      LOG.warn("Invalid price value: {}", term.value());
      return null;
    }
  }

  /**
   * Translates date-related search terms.
   */
  private Predicate translateDateTerm(SearchTerm term, 
                                     Path<OffsetDateTime> path,
                                     CriteriaBuilder criteriaBuilder) {
    try {
      if (term.isRange()) {
        var startDate = LocalDate.parse(term.getRangeStart()).atStartOfDay().atOffset(ZoneOffset.UTC);
        var endDate = LocalDate.parse(term.getRangeEnd()).plusDays(1L).atStartOfDay().atOffset(ZoneOffset.UTC);
        return criteriaBuilder.between(path, startDate, endDate);
      } else {
        var date = LocalDate.parse(term.value()).atStartOfDay().atOffset(ZoneOffset.UTC);
        var nextDay = date.plusDays(1L);
        
        return switch (term.operator()) {
          case EQUALS -> criteriaBuilder.between(path, date, nextDay);
          case GREATER_THAN -> criteriaBuilder.greaterThan(path, nextDay);
          case LESS_THAN -> criteriaBuilder.lessThan(path, date);
          case GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(path, date);
          case LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThan(path, nextDay);
          default -> {
            LOG.warn("Unsupported operator {} for date field", term.operator());
            yield null;
          }
        };
      }
    } catch (DateTimeParseException e) {
      LOG.warn("Invalid date value: {}", term.value());
      return null;
    }
  }

  /**
   * Translates category-related search terms.
   */
  private Predicate translateCategoryTerm(SearchTerm term, 
                                         Root<ProductEntity> root,
                                         CriteriaBuilder criteriaBuilder) {
    var categoryJoin = root.join("category");
    return switch (term.operator()) {
      case EQUALS -> criteriaBuilder.equal(categoryJoin.get("slug"), term.value());
      case CONTAINS -> criteriaBuilder.like(
          criteriaBuilder.lower(categoryJoin.get("title")), 
          "%" + term.value().toLowerCase() + "%"
      );
      default -> {
        LOG.warn("Unsupported operator {} for category field", term.operator());
        yield null;
      }
    };
  }

  /**
   * Translates tags-related search terms.
   */
  private Predicate translateTagsTerm(SearchTerm term, 
                                     Root<ProductEntity> root,
                                     CriteriaBuilder criteriaBuilder) {
    // This would need proper implementation for collection searching
    LOG.warn("Tags search not fully implemented yet: {}", term);
    return null;
  }

  /**
   * Translates full-text search terms across multiple fields.
   */
  private Predicate translateFullTextSearch(SearchTerm term, 
                                           Root<ProductEntity> root,
                                           CriteriaBuilder criteriaBuilder) {
    var searchPattern = "%" + term.value().toLowerCase() + "%";
    
    // Search across title, description, and slug
    var titlePredicate = criteriaBuilder.like(
        criteriaBuilder.lower(root.get("title")), searchPattern);
    var descriptionPredicate = criteriaBuilder.like(
        criteriaBuilder.lower(root.get("description")), searchPattern);
    var slugPredicate = criteriaBuilder.like(
        criteriaBuilder.lower(root.get("slug")), searchPattern);
    
    return criteriaBuilder.or(titlePredicate, descriptionPredicate, slugPredicate);
  }
}