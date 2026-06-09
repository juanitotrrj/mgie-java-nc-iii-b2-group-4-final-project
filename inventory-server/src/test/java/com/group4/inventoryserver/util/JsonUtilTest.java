package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import com.google.gson.reflect.TypeToken;
import com.group4.inventoryserver.dto.auth.LoginRequest;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class JsonUtilTest {

  @Test
  public void toJson_serializesObject() {
    LoginRequest req = new LoginRequest();
    req.setUsername("admin");
    req.setPassword("pass");
    req.setRole("Administrator");

    String json = JsonUtil.toJson(req);

    assertTrue(json.contains("\"username\":\"admin\""));
    assertTrue(json.contains("\"role\":\"Administrator\""));
  }

  @Test
  public void fromJson_deserializesObject() {
    String json = "{\"username\":\"clerk\",\"password\":\"x\",\"role\":\"Cashier\"}";

    LoginRequest req = JsonUtil.fromJson(json, LoginRequest.class);

    assertEquals("clerk", req.getUsername());
    assertEquals("Cashier", req.getRole());
  }

  @Test(expected = IllegalArgumentException.class)
  public void fromJson_throwsOnInvalidJson() {
    JsonUtil.fromJson("{not valid", LoginRequest.class);
  }

  @Test
  public void toPrettyJson_includesFormatting() {
    Map<String, String> map =
        JsonUtil.fromJson("{\"a\":\"1\"}", new TypeToken<Map<String, String>>() {}.getType());

    String pretty = JsonUtil.toPrettyJson(map);

    assertTrue(pretty.contains("\n"));
    assertTrue(pretty.contains("\"a\""));
  }

  @Test
  public void roundTrip_listType() {
    String json = "[\"A\",\"B\"]";
    List<String> list = JsonUtil.fromJson(json, new TypeToken<List<String>>() {}.getType());

    assertEquals(2, list.size());
    assertEquals("[\"A\",\"B\"]", JsonUtil.toJson(list));
  }
}
