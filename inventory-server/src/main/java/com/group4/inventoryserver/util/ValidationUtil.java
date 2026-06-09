package com.group4.inventoryserver.util;

import com.group4.inventoryserver.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidationUtil {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private ValidationUtil() {}

  public static void requireNonBlank(String value, String field, List<FieldError> errors) {
    if (value == null || value.trim().isEmpty()) {
      errors.add(new FieldError(field, capitalize(field) + " is required."));
    }
  }

  public static void requireMaxLength(
      String value, String field, int max, List<FieldError> errors) {
    if (value != null && value.length() > max) {
      errors.add(
          new FieldError(field, capitalize(field) + " must not exceed " + max + " characters."));
    }
  }

  public static void requireValidEmail(String value, String field, List<FieldError> errors) {
    if (value != null
        && !value.trim().isEmpty()
        && !EMAIL_PATTERN.matcher(value.trim()).matches()) {
      errors.add(new FieldError(field, "Invalid email format."));
    }
  }

  public static void throwIfErrors(List<FieldError> errors) {
    if (!errors.isEmpty()) {
      throw new ValidationException("Validation failed.", errors);
    }
  }

  public static List<FieldError> newErrorList() {
    return new ArrayList<>();
  }

  private static String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  public static class FieldError {
    private final String field;
    private final String message;

    public FieldError(String field, String message) {
      this.field = field;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public String getMessage() {
      return message;
    }
  }
}
