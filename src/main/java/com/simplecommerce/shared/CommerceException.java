package com.simplecommerce.shared;

/**
 * @author julius.krah
 */
public class CommerceException extends RuntimeException {

  public CommerceException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommerceException(Throwable cause) {
    super(cause);
  }

  public CommerceException() {
  }
}
