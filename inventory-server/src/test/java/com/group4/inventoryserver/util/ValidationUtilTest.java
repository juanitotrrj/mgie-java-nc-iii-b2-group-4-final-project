package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.List;
import org.junit.Test;

public class ValidationUtilTest {

  @Test
  public void requireNonBlank_addsErrorForNull() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(null, "username", errors);
    assertEquals(1, errors.size());
    assertEquals("username", errors.get(0).getField());
    assertTrue(errors.get(0).getMessage().contains("required"));
  }

  @Test
  public void requireNonBlank_addsErrorForEmptyString() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank("", "password", errors);
    assertEquals(1, errors.size());
    assertEquals("password", errors.get(0).getField());
  }

  @Test
  public void requireNonBlank_addsErrorForWhitespaceOnly() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank("   ", "role", errors);
    assertEquals(1, errors.size());
  }

  @Test
  public void requireNonBlank_noErrorForValidInput() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank("admin", "username", errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void requireMaxLength_addsErrorWhenExceeded() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength("abcdef", "field", 5, errors);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("5"));
  }

  @Test
  public void requireMaxLength_noErrorAtExactLimit() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength("abcde", "field", 5, errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void requireMaxLength_noErrorForNull() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength(null, "field", 5, errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void requireValidEmail_addsErrorForInvalidEmail() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireValidEmail("not-an-email", "email", errors);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("email"));
  }

  @Test
  public void requireValidEmail_noErrorForValidEmail() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireValidEmail("user@example.com", "email", errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void requireValidEmail_noErrorForNullOrBlank() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireValidEmail(null, "email", errors);
    ValidationUtil.requireValidEmail("", "email", errors);
    assertTrue(errors.isEmpty());
  }

  @Test(expected = ValidationException.class)
  public void throwIfErrors_throwsWhenErrorsExist() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    errors.add(new FieldError("field", "error message"));
    ValidationUtil.throwIfErrors(errors);
  }

  @Test
  public void throwIfErrors_noExceptionWhenEmpty() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.throwIfErrors(errors);
  }

  @Test
  public void multipleErrors_accumulateCorrectly() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(null, "username", errors);
    ValidationUtil.requireNonBlank(null, "password", errors);
    ValidationUtil.requireNonBlank(null, "role", errors);
    assertEquals(3, errors.size());
  }
}
