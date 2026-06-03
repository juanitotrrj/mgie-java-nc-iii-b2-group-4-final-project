package com.group4.inventoryclient.api;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthApiClient {

  private final ApiClient client;

  public AuthApiClient(ApiClient client) {
    this.client = client;
  }

  public JsonObject login(String username, String password) throws IOException {
    Map<String, String> body = new HashMap<>();
    body.put("username", username);
    body.put("password", password);
    ApiClient.ApiResponse response = client.post("/auth/login", body);
    if (!response.isSuccess()) {
      throw new IOException(response.getErrorMessage());
    }
    return response.getDataAsObject();
  }

  public void logout() throws IOException {
    client.post("/auth/logout", new HashMap<>());
  }

  public JsonObject me() throws IOException {
    ApiClient.ApiResponse response = client.get("/auth/me");
    if (!response.isSuccess()) {
      throw new IOException(response.getErrorMessage());
    }
    return response.getDataAsObject();
  }
}
