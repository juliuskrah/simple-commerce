package com.simplecommerce.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Parser for search queries using GitHub-style search syntax.
 * 
 * Supported syntax:
 * - Field queries: status:published, price:100, category:electronics
 * - Range queries: price:100..200, created:2024-01-01..2024-12-31
 * - Quoted strings: title:"wireless headphones"
 * - Boolean operators: status:published AND category:electronics
 * - Grouping: (status:published OR status:draft) AND category:electronics
 * 
 * Examples:
 * - status:published price:100..200
 * - category:electronics AND (status:published OR status:draft)
 * - title:"wireless headphones" AND price:>100
 * 
 * @author julius.krah
 * @since 1.0
 */
@Service
public class SearchQueryParser {

  private static final Logger LOG = LoggerFactory.getLogger(SearchQueryParser.class);

  // Regex patterns for different query components
  private static final Pattern FIELD_VALUE_PATTERN = Pattern.compile("(\\w+):(\\w+)");
  private static final Pattern FIELD_QUOTED_PATTERN = Pattern.compile("(\\w+):\"([^\"]+)\"");
  private static final Pattern FIELD_RANGE_PATTERN = Pattern.compile("(\\w+):(\\w+)\\.\\.(\\w+)");
  private static final Pattern FIELD_COMPARISON_PATTERN = Pattern.compile("(\\w+):([><=]+)(\\w+)");

  /**
   * Parses a search query string into a structured SearchQuery object.
   * 
   * @param queryString the raw search query string
   * @return parsed SearchQuery object
   */
  public SearchQuery parse(String queryString) {
    LOG.debug("Parsing search query: {}", queryString);
    
    if (queryString == null || queryString.trim().isEmpty()) {
      return new SearchQuery(new ArrayList<>(), SearchQuery.Operator.AND);
    }

    var terms = new ArrayList<SearchTerm>();
    var cleanQuery = queryString.trim();

    // For now, implement a simple parser that handles basic field:value syntax
    // This can be enhanced with proper ANTLR parsing later
    
    // Split by AND/OR while preserving the operators
    var segments = splitPreservingOperators(cleanQuery);
    var operator = detectPrimaryOperator(cleanQuery);

    for (String segment : segments) {
      if (isOperator(segment)) {
        continue; // Skip operator segments
      }
      
      var term = parseSegment(segment.trim());
      if (term != null) {
        terms.add(term);
      }
    }

    var result = new SearchQuery(terms, operator);
    LOG.debug("Parsed search query: {}", result);
    return result;
  }

  /**
   * Parses a single segment of the query into a SearchTerm.
   */
  private SearchTerm parseSegment(String segment) {
    // Try quoted field pattern first
    Matcher quotedMatcher = FIELD_QUOTED_PATTERN.matcher(segment);
    if (quotedMatcher.matches()) {
      return new SearchTerm(
          quotedMatcher.group(1),
          SearchTerm.Operator.EQUALS,
          quotedMatcher.group(2)
      );
    }

    // Try range pattern
    Matcher rangeMatcher = FIELD_RANGE_PATTERN.matcher(segment);
    if (rangeMatcher.matches()) {
      return new SearchTerm(
          rangeMatcher.group(1),
          SearchTerm.Operator.RANGE,
          rangeMatcher.group(2) + ".." + rangeMatcher.group(3)
      );
    }

    // Try comparison pattern
    Matcher comparisonMatcher = FIELD_COMPARISON_PATTERN.matcher(segment);
    if (comparisonMatcher.matches()) {
      var operatorStr = comparisonMatcher.group(2);
      var operator = switch (operatorStr) {
        case ">" -> SearchTerm.Operator.GREATER_THAN;
        case "<" -> SearchTerm.Operator.LESS_THAN;
        case ">=" -> SearchTerm.Operator.GREATER_THAN_OR_EQUAL;
        case "<=" -> SearchTerm.Operator.LESS_THAN_OR_EQUAL;
        default -> SearchTerm.Operator.EQUALS;
      };
      return new SearchTerm(
          comparisonMatcher.group(1),
          operator,
          comparisonMatcher.group(3)
      );
    }

    // Try simple field:value pattern
    Matcher fieldValueMatcher = FIELD_VALUE_PATTERN.matcher(segment);
    if (fieldValueMatcher.matches()) {
      return new SearchTerm(
          fieldValueMatcher.group(1),
          SearchTerm.Operator.EQUALS,
          fieldValueMatcher.group(2)
      );
    }

    // If no pattern matches, treat as a general text search
    if (!segment.isEmpty()) {
      return new SearchTerm("text", SearchTerm.Operator.CONTAINS, segment);
    }

    return null;
  }

  /**
   * Splits the query string while preserving AND/OR operators.
   */
  private List<String> splitPreservingOperators(String query) {
    var parts = new ArrayList<String>();
    var currentPart = new StringBuilder();
    var tokens = query.split("\\s+");

    for (String token : tokens) {
      if (isOperator(token)) {
        if (currentPart.length() > 0) {
          parts.add(currentPart.toString().trim());
          currentPart = new StringBuilder();
        }
        parts.add(token);
      } else {
        if (currentPart.length() > 0) {
          currentPart.append(" ");
        }
        currentPart.append(token);
      }
    }

    if (currentPart.length() > 0) {
      parts.add(currentPart.toString().trim());
    }

    return parts;
  }

  /**
   * Detects the primary operator in the query (AND vs OR).
   */
  private SearchQuery.Operator detectPrimaryOperator(String query) {
    if (query.contains(" OR ")) {
      return SearchQuery.Operator.OR;
    }
    return SearchQuery.Operator.AND; // Default to AND
  }

  /**
   * Checks if a token is a boolean operator.
   */
  private boolean isOperator(String token) {
    return "AND".equals(token) || "OR".equals(token) || "NOT".equals(token);
  }

  /**
   * Validates a search query for syntax errors.
   * 
   * @param queryString the query to validate
   * @return validation result with any error messages
   */
  public SearchQueryValidation validate(String queryString) {
    var errors = new ArrayList<String>();
    
    if (queryString == null) {
      errors.add("Query cannot be null");
      return new SearchQueryValidation(false, errors);
    }

    if (queryString.trim().isEmpty()) {
      return new SearchQueryValidation(true, errors); // Empty queries are valid
    }

    // Check for unclosed quotes
    long quoteCount = queryString.chars().filter(ch -> ch == '"').count();
    if (quoteCount % 2 != 0) {
      errors.add("Unclosed quote in query");
    }

    // Check for unmatched parentheses
    long openParens = queryString.chars().filter(ch -> ch == '(').count();
    long closeParens = queryString.chars().filter(ch -> ch == ')').count();
    if (openParens != closeParens) {
      errors.add("Unmatched parentheses in query");
    }

    // Additional validations can be added here

    return new SearchQueryValidation(errors.isEmpty(), errors);
  }
}