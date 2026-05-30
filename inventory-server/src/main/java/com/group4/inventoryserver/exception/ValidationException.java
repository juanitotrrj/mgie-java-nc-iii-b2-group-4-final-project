package com.group4.inventoryserver.exception;

public class ValidationException extends ApiException {

  public ValidationException(String message) {
    super(422, message);
  }
}
