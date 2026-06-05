package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class GuestApiIT extends ApiITBase {

  @Test
  public void welcome_is_public() throws Exception {
    assertThat(client.get("/public/welcome").status, is(200));
  }

  @Test
  public void about_is_public() throws Exception {
    E2eHttpClient.HttpResult result = client.get("/public/about");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("description"), is(true));
  }

  @Test
  public void contact_is_public() throws Exception {
    E2eHttpClient.HttpResult result = client.get("/public/contact");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("email"), is(true));
  }

  @Test
  public void submit_inquiry_returns_created() throws Exception {
    JsonObject body = new JsonObject();
    body.addProperty("name", "Test Guest");
    body.addProperty("email", "guest@example.com");
    body.addProperty("subject", "API test");
    body.addProperty("message", "Hello from API test");
    E2eHttpClient.HttpResult result = client.post("/public/inquiries", body.toString());
    assertThat(result.status, is(201));
  }
}
