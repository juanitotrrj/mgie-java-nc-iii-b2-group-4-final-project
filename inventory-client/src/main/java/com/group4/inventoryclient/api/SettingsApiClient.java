package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class SettingsApiClient {

  private final ApiClient apiClient;

  public SettingsApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse get(String section) throws IOException {
    return apiClient.get("/settings?section=" + section);
  }

  public ApiClient.ApiResponse update(String section, Map<String, Object> body) throws IOException {
    return apiClient.put("/settings/" + section, body);
  }

  public ApiClient.ApiResponse testConnection() throws IOException {
    return apiClient.get("/settings/test-connection");
  }

  public ApiClient.ApiResponse backup() throws IOException {
    return apiClient.post("/settings/backup", null);
  }
}
