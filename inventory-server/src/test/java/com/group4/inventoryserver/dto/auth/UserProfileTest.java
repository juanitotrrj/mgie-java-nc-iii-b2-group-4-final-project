package com.group4.inventoryserver.dto.auth;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class UserProfileTest {

  @Test
  public void constructor_setsAllFields() {
    List<String> permissions = Arrays.asList("AUTH_LOGIN", "DASHBOARD_VIEW", "PRODUCT_READ");
    UserProfile profile =
        new UserProfile(
            1L,
            "System Administrator",
            "admin",
            "admin@inventory.local",
            "Administrator",
            "Active",
            "2026-06-01T12:00:00Z",
            permissions);

    assertEquals(1L, profile.getUserId());
    assertEquals("System Administrator", profile.getFullName());
    assertEquals("admin", profile.getUsername());
    assertEquals("admin@inventory.local", profile.getEmail());
    assertEquals("Administrator", profile.getRole());
    assertEquals("Active", profile.getStatus());
    assertEquals("2026-06-01T12:00:00Z", profile.getLastLogin());
    assertEquals(3, profile.getPermissions().size());
    assertTrue(profile.getPermissions().contains("PRODUCT_READ"));
  }

  @Test
  public void nullLastLogin_isAllowed() {
    UserProfile profile =
        new UserProfile(
            2L,
            "Test User",
            "testuser",
            "test@test.com",
            "Cashier",
            "Active",
            null,
            Collections.<String>emptyList());
    assertNull(profile.getLastLogin());
    assertTrue(profile.getPermissions().isEmpty());
  }
}
