package com.group4.inventoryserver.exception;

public class NotFoundException extends ApiException {

  public NotFoundException(String message) {
    super(404, message);
  }

  public NotFoundException(String resource, Object id) {
    this(resource + " not found with id: " + id);
  }
}
