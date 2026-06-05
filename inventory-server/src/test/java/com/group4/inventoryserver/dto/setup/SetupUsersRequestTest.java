package com.group4.inventoryserver.dto.setup;

import static org.junit.Assert.*;

import com.group4.inventoryserver.dto.setup.SetupUsersRequest.SetupUserEntry;
import com.group4.inventoryserver.util.JsonUtil;
import java.util.Collections;
import org.junit.Test;

public class SetupUsersRequestTest {

  @Test
  public void gettersAndSetters_workCorrectly() {
    SetupUserEntry user = new SetupUserEntry();
    user.setUsername("admin");
    user.setPassword("Admin@123");
    user.setEmail("admin@test.com");
    user.setFullName("System Admin");
    user.setRole("Administrator");

    SetupUsersRequest req = new SetupUsersRequest();
    req.setUsers(Collections.singletonList(user));

    assertEquals(1, req.getUsers().size());
    SetupUserEntry stored = req.getUsers().get(0);
    assertEquals("admin", stored.getUsername());
    assertEquals("Admin@123", stored.getPassword());
    assertEquals("admin@test.com", stored.getEmail());
    assertEquals("System Admin", stored.getFullName());
    assertEquals("Administrator", stored.getRole());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    SetupUserEntry user = new SetupUserEntry();
    user.setUsername("clerk1");
    user.setRole("Cashier");

    SetupUsersRequest original = new SetupUsersRequest();
    original.setUsers(Collections.singletonList(user));

    SetupUsersRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(original), SetupUsersRequest.class);

    assertEquals("clerk1", parsed.getUsers().get(0).getUsername());
    assertEquals("Cashier", parsed.getUsers().get(0).getRole());
  }
}
