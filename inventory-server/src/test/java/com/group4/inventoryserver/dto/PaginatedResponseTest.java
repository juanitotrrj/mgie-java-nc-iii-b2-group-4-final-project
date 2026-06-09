package com.group4.inventoryserver.dto;

import static org.junit.Assert.*;

import com.group4.inventoryserver.util.JsonUtil;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class PaginatedResponseTest {

  @Test
  public void defaultConstructor_setsSuccessMessage() {
    PaginationMeta meta = new PaginationMeta(1, 10, 0, "createdAt", "desc");
    PaginatedResponse<String> response =
        new PaginatedResponse<>(Collections.<String>emptyList(), meta);

    assertTrue(response.isSuccess());
    assertEquals("Records retrieved successfully.", response.getMessage());
    assertTrue(response.getData().isEmpty());
    assertEquals(meta, response.getMeta());
  }

  @Test
  public void customMessageConstructor() {
    PaginationMeta meta = new PaginationMeta(2, 5, 12, "name", "asc");
    PaginatedResponse<String> response =
        new PaginatedResponse<>("Filtered results.", Arrays.asList("A", "B"), meta);

    assertEquals("Filtered results.", response.getMessage());
    assertEquals(2, response.getData().size());
    assertEquals(3, response.getMeta().getTotalPages());
  }

  @Test
  public void serializesToJson() {
    PaginationMeta meta = new PaginationMeta(1, 10, 1, "id", "asc");
    PaginatedResponse<String> response =
        new PaginatedResponse<>(Collections.singletonList("x"), meta);

    String json = JsonUtil.toJson(response);

    assertTrue(json.contains("\"success\":true"));
    assertTrue(json.contains("\"totalRecords\":1"));
  }
}
