package com.simplecommerce.search;

/**
 * Represents a single search term with field, operator, and value.
 * 
 * Examples:
 * - Field equals: status:published -> SearchTerm("status", EQUALS, "published")
 * - Range: price:100..200 -> SearchTerm("price", RANGE, "100..200")
 * - Comparison: price:>100 -> SearchTerm("price", GREATER_THAN, "100")
 * - Text search: wireless -> SearchTerm("text", CONTAINS, "wireless")
 * 
 * @param field the field name to search in
 * @param operator the comparison operator
 * @param value the value to search for
 * 
 * @author julius.krah
 * @since 1.0
 */
public record SearchTerm(String field, Operator operator, String value) {

  public enum Operator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN_OR_EQUAL,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    RANGE,
    IN,
    NOT_IN
  }

  /**
   * Checks if this is a range query (e.g., price:100..200).
   */
  public boolean isRange() {
    return operator == Operator.RANGE && value.contains("..");
  }

  /**
   * Gets the start value for range queries.
   */
  public String getRangeStart() {
    if (!isRange()) {
      throw new IllegalStateException("Not a range query");
    }
    return value.split("\\.\\.")[0];
  }

  /**
   * Gets the end value for range queries.
   */
  public String getRangeEnd() {
    if (!isRange()) {
      throw new IllegalStateException("Not a range query");
    }
    var parts = value.split("\\.\\.");
    return parts.length > 1 ? parts[1] : parts[0];
  }

  /**
   * Checks if this is a text search term.
   */
  public boolean isTextSearch() {
    return "text".equals(field) || operator == Operator.CONTAINS;
  }

  /**
   * Checks if this term matches a specific field name.
   */
  public boolean isForField(String fieldName) {
    return field.equals(fieldName);
  }

  @Override
  public String toString() {
    return switch (operator) {
      case RANGE -> field + ":" + value;
      case GREATER_THAN -> field + ":>" + value;
      case LESS_THAN -> field + ":<" + value;
      case GREATER_THAN_OR_EQUAL -> field + ":>=" + value;
      case LESS_THAN_OR_EQUAL -> field + ":<=" + value;
      case NOT_EQUALS -> field + ":!" + value;
      case CONTAINS -> field + ":*" + value + "*";
      default -> field + ":" + value;
    };
  }
}