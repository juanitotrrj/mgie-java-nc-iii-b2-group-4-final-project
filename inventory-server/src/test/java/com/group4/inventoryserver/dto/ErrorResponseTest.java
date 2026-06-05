package com.group4.inventoryserver.dto;

import static org.junit.Assert.*;

import com.group4.inventoryserver.util.JsonUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Collections;
import org.junit.Test;

public class ErrorResponseTest {

  @Test
  public void statusConstructor_setsMessage() {
    ErrorResponse response = new ErrorResponse(404, "Not found");

    assertFalse(response.isSuccess());
    assertEquals("Not found", response.getMessage());
    assertNull(response.getErrors());
  }

  @Test
  public void fieldErrorsConstructor_setsErrors() {
    FieldError error = new FieldError("username", "Username is required.");
    ErrorResponse response =
        new ErrorResponse("Validation failed.", Collections.singletonList(error));

    assertFalse(response.isSuccess());
    assertEquals(1, response.getErrors().size());
    assertEquals("username", response.getErrors().get(0).getField());
  }

  @Test
  public void serializesToJson() {
    ErrorResponse response = new ErrorResponse(403, "Forbidden");

    String json = JsonUtil.toJson(response);

    assertTrue(json.contains("\"success\":false"));
    assertTrue(json.contains("Forbidden"));
  }
}
