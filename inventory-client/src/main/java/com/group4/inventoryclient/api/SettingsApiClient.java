package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class SettingsApiClient {

  private final ApiClient apiClient;

  public SettingsApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse getAll() throws IOException {
    return apiClient.get("/settings");
  }

  public ApiClient.ApiResponse updateCompany(Map<String, Object> body) throws IOException {
    return apiClient.put("/settings/company", body);
  }

  public ApiClient.ApiResponse updateInventory(Map<String, Object> body) throws IOException {
    return apiClient.put("/settings/inventory", body);
  }

  public ApiClient.ApiResponse updateSecurity(Map<String, Object> body) throws IOException {
    return apiClient.put("/settings/security", body);
  }

  public ApiClient.ApiResponse updateNotifications(Map<String, Object> body) throws IOException {
    return apiClient.put("/settings/notifications", body);
  }

  public ApiClient.ApiResponse testConnection() throws IOException {
    return apiClient.get("/settings/database/test");
  }

  public ApiClient.ApiResponse backup() throws IOException {
    return apiClient.post("/settings/database/backup", null);
  }
}
