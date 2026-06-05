package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class UsersApiIT extends ApiITBase {

  @Test
  public void list_users_returns_200() throws Exception {
    assertThat(adminClient.get("/users?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void create_user_returns_201() throws Exception {
    JsonObject body = new JsonObject();
    String username = "apiuser" + System.currentTimeMillis();
    body.addProperty("fullName", "API Test User");
    body.addProperty("username", username);
    body.addProperty("email", username + "@test.local");
    body.addProperty("role", "Cashier");
    body.addProperty("password", "Test@1234");
    body.addProperty("status", "Active");

    E2eHttpClient.HttpResult result = adminClient.post("/users", body.toString());
    assertThat(result.status, is(201));
    assertThat(
        result.jsonRoot().getAsJsonObject("data").get("username").getAsString(), is(username));
  }
}
