package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class PurchaseApiClient {

  private final ApiClient apiClient;

  public PurchaseApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse list(int page, String search, String status) throws IOException {
    StringBuilder path = new StringBuilder("/purchases?page=" + page);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(urlEncode(search));
    }
    if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("All")) {
      path.append("&status=").append(urlEncode(status));
    }
    return apiClient.get(path.toString());
  }

  public ApiClient.ApiResponse get(long id) throws IOException {
    return apiClient.get("/purchases/" + id);
  }

  public ApiClient.ApiResponse create(Map<String, Object> body) throws IOException {
    return apiClient.post("/purchases", body);
  }

  public ApiClient.ApiResponse receive(long id, Map<String, Object> body) throws IOException {
    return apiClient.post("/purchases/" + id + "/receive", body);
  }

  public ApiClient.ApiResponse cancel(long id, Map<String, Object> body) throws IOException {
    return apiClient.post("/purchases/" + id + "/cancel", body);
  }

  private String urlEncode(String value) {
    try {
      return java.net.URLEncoder.encode(value, "UTF-8");
    } catch (Exception e) {
      return value;
    }
  }
}
