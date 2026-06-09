package com.group4.inventoryserver.dto.auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoginRequestTest {

  @Test
  public void gettersAndSetters_workCorrectly() {
    LoginRequest request = new LoginRequest();
    request.setUsername("admin");
    request.setPassword("Admin@123");
    request.setRole("Administrator");

    assertEquals("admin", request.getUsername());
    assertEquals("Admin@123", request.getPassword());
    assertEquals("Administrator", request.getRole());
  }

  @Test
  public void defaultValues_areNull() {
    LoginRequest request = new LoginRequest();
    assertNull(request.getUsername());
    assertNull(request.getPassword());
    assertNull(request.getRole());
  }
}
