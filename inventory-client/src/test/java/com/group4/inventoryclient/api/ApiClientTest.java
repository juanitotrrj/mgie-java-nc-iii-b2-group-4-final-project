package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;

public class ApiClientTest {

  @Test
  public void apiResponse_parses_success_payload() {
    String json =
        "{\"success\":true,\"data\":{\"token\":\"abc\",\"user\":{\"username\":\"admin\"}},\"message\":\"OK\"}";
    ApiClient.ApiResponse response = new ApiClient.ApiResponse(200, json);
    assertThat(response.isSuccess(), is(true));
    JsonObject data = response.getDataAsObject();
    assertThat(data.get("token").getAsString(), is("abc"));
  }

  @Test
  public void apiResponse_extracts_error_message() {
    ApiClient.ApiResponse response =
        new ApiClient.ApiResponse(400, "{\"message\":\"Invalid credentials\"}");
    assertThat(response.getErrorMessage(), is("Invalid credentials"));
  }

  @Test
  public void apiResponse_extracts_error_field() {
    ApiClient.ApiResponse response = new ApiClient.ApiResponse(500, "{\"error\":\"Server error\"}");
    assertThat(response.getErrorMessage(), is("Server error"));
  }

  @Test
  public void apiResponse_parses_data_array_and_meta() {
    String json =
        "{\"data\":[{\"id\":1}],\"meta\":{\"page\":1,\"totalPages\":2,\"totalItems\":10}}";
    ApiClient.ApiResponse response = new ApiClient.ApiResponse(200, json);
    JsonArray array = response.getDataAsArray();
    assertThat(array.size(), is(1));
    JsonObject meta = response.getMeta();
    assertThat(meta.get("page").getAsInt(), is(1));
    assertThat(meta.get("totalItems").getAsInt(), is(10));
  }

  @Test
  public void apiResponse_getRawRoot() {
    ApiClient.ApiResponse response =
        new ApiClient.ApiResponse(200, "{\"success\":true,\"data\":{\"x\":1}}");
    assertThat(response.getRawRoot().get("success").getAsBoolean(), is(true));
  }

  @Test
  public void apiResponse_failureStatus() {
    ApiClient.ApiResponse response = new ApiClient.ApiResponse(404, "{}");
    assertThat(response.isSuccess(), is(false));
  }

  @Test
  public void get_delegatesWithGetMethod() throws Exception {
    TestableApiClient client = new TestableApiClient();
    client.nextResponse = new ApiClient.ApiResponse(200, "{\"data\":{}}");
    ApiClient.ApiResponse response = client.get("/products");
    assertThat(client.lastMethod, is("GET"));
    assertThat(client.lastPath, is("/products"));
    assertThat(client.lastBody, is(nullValue()));
    assertThat(response.getStatusCode(), is(200));
  }

  @Test
  public void post_delegatesWithBody() throws Exception {
    TestableApiClient client = new TestableApiClient();
    java.util.Map<String, String> body = new java.util.HashMap<>();
    body.put("username", "admin");
    client.post("/auth/login", body);
    assertThat(client.lastMethod, is("POST"));
    assertThat(client.lastPath, is("/auth/login"));
    assertThat(client.lastBody, is(notNullValue()));
  }

  @Test
  public void put_delegatesWithBody() throws Exception {
    TestableApiClient client = new TestableApiClient();
    java.util.Map<String, Object> body = new java.util.HashMap<>();
    body.put("name", "Widget");
    client.put("/products/1", body);
    assertThat(client.lastMethod, is("PUT"));
    assertThat(client.lastPath, is("/products/1"));
  }

  @Test
  public void delete_delegatesWithDeleteMethod() throws Exception {
    TestableApiClient client = new TestableApiClient();
    client.delete("/products/5");
    assertThat(client.lastMethod, is("DELETE"));
    assertThat(client.lastPath, is("/products/5"));
  }

  @Test
  public void bearerAndSetupToken_gettersAndSetters() {
    ApiClient client = new ApiClient("http://localhost:8080/api");
    assertThat(client.getBaseUrl(), is("http://localhost:8080/api"));
    client.setBearerToken("jwt-token");
    client.setSetupToken("setup-token");
    assertThat(client.getBearerToken(), is("jwt-token"));
    assertThat(client.getSetupToken(), is("setup-token"));
  }
}
