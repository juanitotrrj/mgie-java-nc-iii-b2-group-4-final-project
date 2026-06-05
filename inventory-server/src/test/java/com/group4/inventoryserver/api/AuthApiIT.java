package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class AuthApiIT extends ApiITBase {

  @Test
  public void login_with_admin_credentials_returns_token() throws Exception {
    E2eHttpClient.HttpResult result = client.login("admin", "Admin@123");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().has("data"), is(true));
  }

  @Test
  public void login_with_invalid_password_returns_401() throws Exception {
    E2eHttpClient.HttpResult result = client.login("admin", "wrong-password");
    assertThat(result.status, is(401));
  }

  @Test
  public void login_with_unknown_user_returns_401() throws Exception {
    E2eHttpClient.HttpResult result = client.login("unknown-user", "Admin@123");
    assertThat(result.status, is(401));
  }

  @Test
  public void get_auth_me_returns_current_user() throws Exception {
    E2eHttpClient.HttpResult result = adminClient.get("/auth/me");
    assertThat(result.status, is(200));
    JsonObject data = result.jsonRoot().getAsJsonObject("data");
    assertThat(data.get("username").getAsString(), is("admin"));
  }

  @Test
  public void logout_invalidates_session() throws Exception {
    E2eHttpClient loggedIn = new E2eHttpClient(launcher.getBaseUrl());
    loggedIn.login("admin", "Admin@123");
    assertThat(loggedIn.post("/auth/logout", "{}").status, is(200));
    assertThat(loggedIn.get("/auth/me").status, is(401));
  }
}
