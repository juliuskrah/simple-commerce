package com.simplecommerce.shared.exceptions;

/**
 * @author julius.krah
 */
public class NotFoundException extends CommerceException {
  public NotFoundException(String message) {
    super(message, null);
  }

  public NotFoundException() {}
}
