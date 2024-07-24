package com.simplecommerce.shared;

/**
 * @author julius.krah
 */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException() {}
}
