package com.group4.inventoryserver.dto;

import static org.junit.Assert.*;

import com.group4.inventoryserver.util.JsonUtil;
import org.junit.Test;

public class ApiResponseTest {

  @Test
  public void success_setsDefaults() {
    ApiResponse<String> response = ApiResponse.success("payload");

    assertTrue(response.isSuccess());
    assertEquals("payload", response.getData());
    assertEquals("Request completed successfully.", response.getMessage());
  }

  @Test
  public void success_withCustomMessage() {
    ApiResponse<Integer> response = ApiResponse.success(42, "Created.");

    assertTrue(response.isSuccess());
    assertEquals(Integer.valueOf(42), response.getData());
    assertEquals("Created.", response.getMessage());
  }

  @Test
  public void error_hasNoData() {
    ApiResponse<Object> response = ApiResponse.error("Something failed");

    assertFalse(response.isSuccess());
    assertNull(response.getData());
    assertEquals("Something failed", response.getMessage());
  }

  @Test
  public void serializesToJson() {
    String json = JsonUtil.toJson(ApiResponse.success("ok"));

    assertTrue(json.contains("\"success\":true"));
    assertTrue(json.contains("\"data\":\"ok\""));
  }
}
