package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class IcrApiClient {

  private final ApiClient apiClient;

  public IcrApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse list(int page, String search, String status) throws IOException {
    StringBuilder path = new StringBuilder("/inventory-change-requests?page=" + page);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(urlEncode(search));
    }
    if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("All")) {
      path.append("&status=").append(urlEncode(status));
    }
    return apiClient.get(path.toString());
  }

  public ApiClient.ApiResponse get(long id) throws IOException {
    return apiClient.get("/inventory-change-requests/" + id);
  }

  public ApiClient.ApiResponse create(Map<String, Object> body) throws IOException {
    return apiClient.post("/inventory-change-requests", body);
  }

  public ApiClient.ApiResponse approve(long id, Map<String, Object> body) throws IOException {
    return apiClient.put("/inventory-change-requests/" + id + "/approve", body);
  }

  public ApiClient.ApiResponse reject(long id, Map<String, Object> body) throws IOException {
    return apiClient.put("/inventory-change-requests/" + id + "/reject", body);
  }

  private String urlEncode(String value) {
    try {
      return java.net.URLEncoder.encode(value, "UTF-8");
    } catch (Exception e) {
      return value;
    }
  }
}
