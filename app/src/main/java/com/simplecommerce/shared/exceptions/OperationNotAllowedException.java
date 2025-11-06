package com.simplecommerce.shared.exceptions;

/// @author julius.krah
public class OperationNotAllowedException extends CommerceException {

  public OperationNotAllowedException(String message) {
    super(message);
  }
}
