package com.group4.inventoryclient.api;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupApiClient {

  private final ApiClient client;

  public SetupApiClient(ApiClient client) {
    this.client = client;
  }

  public JsonObject getStatus() throws IOException {
    ApiClient.ApiResponse response = client.get("/setup/status");
    if (!response.isSuccess()) {
      throw new IOException("Failed to get setup status: " + response.getErrorMessage());
    }
    return response.getDataAsObject();
  }

  public String rootLogin(String username, String password) throws IOException {
    Map<String, String> body = new HashMap<>();
    body.put("username", username);
    body.put("password", password);
    ApiClient.ApiResponse response = client.post("/setup/root-login", body);
    if (!response.isSuccess()) {
      throw new IOException("Root login failed: " + response.getErrorMessage());
    }
    JsonObject data = response.getDataAsObject();
    String token = data.has("token") ? data.get("token").getAsString() : null;
    if (token != null) {
      client.setSetupToken(token);
    }
    return token;
  }

  public void saveBusinessSettings(Map<String, Object> settings) throws IOException {
    ApiClient.ApiResponse response = client.put("/setup/business-settings", settings);
    if (!response.isSuccess()) {
      throw new IOException("Failed to save business settings: " + response.getErrorMessage());
    }
  }

  public void seedRoles(List<String> roles) throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("roles", roles);
    ApiClient.ApiResponse response = client.post("/setup/roles/seed", body);
    if (!response.isSuccess()) {
      throw new IOException("Failed to seed roles: " + response.getErrorMessage());
    }
  }

  public void seedPermissions(Map<String, List<String>> rolePermissions) throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("rolePermissions", rolePermissions);
    ApiClient.ApiResponse response = client.post("/setup/permissions/seed", body);
    if (!response.isSuccess()) {
      throw new IOException("Failed to seed permissions: " + response.getErrorMessage());
    }
  }

  public void createUsers(List<Map<String, String>> users) throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("users", users);
    ApiClient.ApiResponse response = client.post("/setup/users", body);
    if (!response.isSuccess()) {
      throw new IOException("Failed to create users: " + response.getErrorMessage());
    }
  }

  public JsonObject finish() throws IOException {
    ApiClient.ApiResponse response = client.post("/setup/finish", new HashMap<>());
    if (!response.isSuccess()) {
      throw new IOException("Failed to finish setup: " + response.getErrorMessage());
    }
    return response.getDataAsObject();
  }

  public JsonObject getProgress() throws IOException {
    ApiClient.ApiResponse response = client.get("/setup/progress");
    if (!response.isSuccess()) {
      throw new IOException("Failed to get progress: " + response.getErrorMessage());
    }
    return response.getDataAsObject();
  }
}
