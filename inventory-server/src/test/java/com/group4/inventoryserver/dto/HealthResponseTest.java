package com.group4.inventoryserver.dto;

import static org.junit.Assert.*;

import com.group4.inventoryserver.dto.HealthResponse.DatabaseStatus;
import com.group4.inventoryserver.util.JsonUtil;
import org.junit.Test;

public class HealthResponseTest {

  @Test
  public void getters_returnConstructorValues() {
    DatabaseStatus db = new DatabaseStatus(true, 12L);
    HealthResponse response =
        new HealthResponse("UP", "G4IMS API", "1.0.0", "development", "2026-01-01T00:00:00Z", db);

    assertEquals("UP", response.getStatus());
    assertEquals("G4IMS API", response.getAppName());
    assertEquals("1.0.0", response.getAppVersion());
    assertEquals("development", response.getAppEnv());
    assertEquals("2026-01-01T00:00:00Z", response.getTimestamp());
    assertTrue(response.getDatabase().isConnected());
    assertEquals(12L, response.getDatabase().getResponseTimeMs());
  }

  @Test
  public void serializesNestedDatabaseStatus() {
    HealthResponse response =
        new HealthResponse(
            "UP", "API", "1.0.0", "test", "2026-01-01T00:00:00Z", new DatabaseStatus(false, 0L));

    String json = JsonUtil.toJson(response);

    assertTrue(json.contains("\"status\":\"UP\""));
    assertTrue(json.contains("\"connected\":false"));
  }
}
