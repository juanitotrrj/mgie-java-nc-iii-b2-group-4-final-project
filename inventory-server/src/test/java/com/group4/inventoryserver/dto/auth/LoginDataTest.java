package com.group4.inventoryserver.dto.auth;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LoginDataTest {

  @Test
  public void constructor_setsAllFields() {
    List<String> perms = Arrays.asList("PRODUCT_READ", "PRODUCT_WRITE");
    UserProfile profile =
        new UserProfile(
            1L, "Admin", "admin", "admin@test.com", "Administrator", "Active", null, perms);
    LoginData data = new LoginData("jwt-token", "Bearer", 1800, profile);

    assertEquals("jwt-token", data.getToken());
    assertEquals("Bearer", data.getTokenType());
    assertEquals(1800, data.getExpiresInSeconds());
    assertNotNull(data.getUser());
    assertEquals("admin", data.getUser().getUsername());
  }
}
