package com.simplecommerce.product.search;

import java.util.List;

/**
 * Represents a parsed search query with terms and logical operators.
 * 
 * @param terms the search terms
 * @param operator the primary logical operator (AND/OR) between terms
 * 
 * @author julius.krah
 * @since 1.0
 */
public record SearchQuery(List<SearchTerm> terms, Operator operator) {

  public enum Operator {
    AND,
    OR,
    NOT
  }

  /**
   * Checks if the query is empty (no terms).
   */
  public boolean isEmpty() {
    return terms == null || terms.isEmpty();
  }

  /**
   * Gets the number of search terms.
   */
  public int size() {
    return terms == null ? 0 : terms.size();
  }

  /**
   * Checks if the query contains a specific field.
   */
  public boolean hasField(String fieldName) {
    return terms != null && terms.stream()
        .anyMatch(term -> term.field().equals(fieldName));
  }

  /**
   * Gets all terms for a specific field.
   */
  public List<SearchTerm> getTermsForField(String fieldName) {
    if (terms == null) {
      return List.of();
    }
    return terms.stream()
        .filter(term -> term.field().equals(fieldName))
        .toList();
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "SearchQuery[empty]";
    }
    return "SearchQuery[terms=" + terms.size() + ", operator=" + operator + "]";
  }
}