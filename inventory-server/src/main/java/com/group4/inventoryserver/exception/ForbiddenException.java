package com.group4.inventoryserver.exception;

public class ForbiddenException extends ApiException {

  public ForbiddenException(String message) {
    super(403, message);
  }

  public ForbiddenException() {
    this("Forbidden");
  }
}
