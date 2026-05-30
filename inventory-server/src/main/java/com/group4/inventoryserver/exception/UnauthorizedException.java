package com.group4.inventoryserver.exception;

public class UnauthorizedException extends ApiException {

  public UnauthorizedException(String message) {
    super(401, message);
  }

  public UnauthorizedException() {
    this("Unauthorized");
  }
}
