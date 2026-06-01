package com.group4.inventoryserver.exception;

import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Collections;
import java.util.List;

public class ValidationException extends ApiException {

  private final List<FieldError> fieldErrors;

  public ValidationException(String message) {
    super(422, message);
    this.fieldErrors = Collections.emptyList();
  }

  public ValidationException(String message, List<FieldError> fieldErrors) {
    super(422, message);
    this.fieldErrors = fieldErrors;
  }

  public List<FieldError> getFieldErrors() {
    return fieldErrors;
  }
}
