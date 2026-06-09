package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Before;
import org.junit.Test;

public class PermissionMatrixIT extends ApiITBase {

  private E2eHttpClient cashierClient;

  @Before
  public void setUpCashier() throws Exception {
    JsonObject body = new JsonObject();
    String username = "cashier" + System.currentTimeMillis();
    body.addProperty("fullName", "API Cashier");
    body.addProperty("username", username);
    body.addProperty("email", username + "@test.local");
    body.addProperty("role", "Cashier");
    body.addProperty("password", "Test@1234");
    body.addProperty("status", "Active");
    adminClient.post("/users", body.toString());

    cashierClient = new E2eHttpClient(launcher.getBaseUrl());
    cashierClient.login(username, "Test@1234");
  }

  @Test
  public void cashier_gets_403_on_users_admin_gets_200() throws Exception {
    assertThat(cashierClient.get("/users?page=1&pageSize=10").status, is(403));
    assertThat(adminClient.get("/users?page=1&pageSize=10").status, is(200));
  }
}
