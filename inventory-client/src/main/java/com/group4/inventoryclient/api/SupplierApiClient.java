package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class SupplierApiClient {

  private final ApiClient client;

  public SupplierApiClient(ApiClient client) {
    this.client = client;
  }

  public ApiClient.ApiResponse list(int page, String search, String status) throws IOException {
    StringBuilder path = new StringBuilder("/suppliers?page=" + page);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(java.net.URLEncoder.encode(search, "UTF-8"));
    }
    if (status != null && !status.isEmpty() && !"All".equals(status)) {
      path.append("&status=").append(status);
    }
    return client.get(path.toString());
  }

  public ApiClient.ApiResponse get(long id) throws IOException {
    return client.get("/suppliers/" + id);
  }

  public ApiClient.ApiResponse create(Map<String, Object> body) throws IOException {
    return client.post("/suppliers", body);
  }

  public ApiClient.ApiResponse update(long id, Map<String, Object> body) throws IOException {
    return client.put("/suppliers/" + id, body);
  }

  public ApiClient.ApiResponse delete(long id) throws IOException {
    return client.delete("/suppliers/" + id);
  }
}
