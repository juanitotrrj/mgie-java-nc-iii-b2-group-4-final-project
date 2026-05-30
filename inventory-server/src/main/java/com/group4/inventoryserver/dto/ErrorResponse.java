package com.group4.inventoryserver.dto;

import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.List;

public class ErrorResponse {

  private final boolean success;
  private final String message;
  private final List<FieldError> errors;

  public ErrorResponse(int status, String message) {
    this.success = false;
    this.message = message;
    this.errors = null;
  }

  public ErrorResponse(String message, List<FieldError> errors) {
    this.success = false;
    this.message = message;
    this.errors = errors;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public List<FieldError> getErrors() {
    return errors;
  }
}
