package com.group4.inventoryserver.dto.user;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.util.JsonUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.List;
import org.junit.Test;

public class UserCreateRequestTest {

  private void validate(UserCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getFullName(), "fullName", errors);
    ValidationUtil.requireNonBlank(request.getUsername(), "username", errors);
    ValidationUtil.requireNonBlank(request.getEmail(), "email", errors);
    ValidationUtil.requireValidEmail(request.getEmail(), "email", errors);
    ValidationUtil.requireNonBlank(request.getRole(), "role", errors);
    ValidationUtil.requireNonBlank(request.getPassword(), "password", errors);
    ValidationUtil.throwIfErrors(errors);
  }

  private UserCreateRequest validRequest() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("John Clerk");
    req.setUsername("jclerk");
    req.setEmail("jclerk@example.com");
    req.setRole("Cashier");
    req.setPassword("Clerk@123");
    req.setStatus("Active");
    return req;
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    UserCreateRequest req = validRequest();

    assertEquals("John Clerk", req.getFullName());
    assertEquals("jclerk", req.getUsername());
    assertEquals("Cashier", req.getRole());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    UserCreateRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(validRequest()), UserCreateRequest.class);

    assertEquals("jclerk", parsed.getUsername());
    assertEquals("Cashier", parsed.getRole());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenUsernameBlank() {
    UserCreateRequest req = validRequest();
    req.setUsername("");
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenEmailInvalid() {
    UserCreateRequest req = validRequest();
    req.setEmail("bad-email");
    validate(req);
  }
}
