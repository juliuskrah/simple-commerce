package com.simplecommerce.shared.exceptions;

/**
 * @author julius.krah
 */
public class CommerceException extends RuntimeException {

  public CommerceException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommerceException(String message) {
    super(message);
  }

  public CommerceException(Throwable cause) {
    super(cause);
  }

  public CommerceException() {
  }
}
