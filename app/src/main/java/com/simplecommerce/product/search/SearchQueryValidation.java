package com.simplecommerce.product.search;

import java.util.List;

/**
 * Result of search query validation.
 * 
 * @param valid true if the query is syntactically valid
 * @param errors list of validation error messages
 * 
 * @author julius.krah
 * @since 1.0
 */
public record SearchQueryValidation(boolean valid, List<String> errors) {

  /**
   * Creates a successful validation result.
   */
  public static SearchQueryValidation success() {
    return new SearchQueryValidation(true, List.of());
  }

  /**
   * Creates a failed validation result with a single error.
   */
  public static SearchQueryValidation error(String errorMessage) {
    return new SearchQueryValidation(false, List.of(errorMessage));
  }

  /**
   * Creates a failed validation result with multiple errors.
   */
  public static SearchQueryValidation errors(List<String> errorMessages) {
    return new SearchQueryValidation(false, errorMessages);
  }

  /**
   * Gets the first error message, if any.
   */
  public String getFirstError() {
    return errors.isEmpty() ? null : errors.getFirst();
  }

  /**
   * Gets all error messages joined with newlines.
   */
  public String getAllErrors() {
    return String.join("\n", errors);
  }
}