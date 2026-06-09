package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class UserApiClient {

  private final ApiClient apiClient;

  public UserApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse list(int page, String search, String role, String status)
      throws IOException {
    StringBuilder path = new StringBuilder("/users?page=").append(page);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(search);
    }
    if (role != null && !role.isEmpty() && !role.equals("All")) {
      path.append("&role=").append(role);
    }
    if (status != null && !status.isEmpty() && !status.equals("All")) {
      path.append("&status=").append(status);
    }
    return apiClient.get(path.toString());
  }

  public ApiClient.ApiResponse get(long id) throws IOException {
    return apiClient.get("/users/" + id);
  }

  public ApiClient.ApiResponse create(Map<String, Object> body) throws IOException {
    return apiClient.post("/users", body);
  }

  public ApiClient.ApiResponse update(long id, Map<String, Object> body) throws IOException {
    return apiClient.put("/users/" + id, body);
  }

  public ApiClient.ApiResponse deactivate(long id) throws IOException {
    return apiClient.delete("/users/" + id);
  }

  public ApiClient.ApiResponse resetPassword(long id, Map<String, Object> body) throws IOException {
    return apiClient.post("/users/" + id + "/reset-password", body);
  }
}
