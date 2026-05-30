package com.group4.inventoryserver.exception;

public class ConflictException extends ApiException {

  public ConflictException(String message) {
    super(409, message);
  }
}
