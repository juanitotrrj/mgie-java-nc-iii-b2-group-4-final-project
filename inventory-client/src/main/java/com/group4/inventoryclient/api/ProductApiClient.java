package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class ProductApiClient {

  private final ApiClient client;

  public ProductApiClient(ApiClient client) {
    this.client = client;
  }

  public ApiClient.ApiResponse list(int page, String search, String status, String category)
      throws IOException {
    StringBuilder path = new StringBuilder("/products?page=" + page);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(java.net.URLEncoder.encode(search, "UTF-8"));
    }
    if (status != null && !status.isEmpty() && !"All".equals(status)) {
      path.append("&status=").append(status);
    }
    if (category != null && !category.isEmpty() && !"All".equals(category)) {
      path.append("&category=").append(category);
    }
    return client.get(path.toString());
  }

  public ApiClient.ApiResponse get(long id) throws IOException {
    return client.get("/products/" + id);
  }

  public ApiClient.ApiResponse create(Map<String, Object> body) throws IOException {
    return client.post("/products", body);
  }

  public ApiClient.ApiResponse update(long id, Map<String, Object> body) throws IOException {
    return client.put("/products/" + id, body);
  }

  public ApiClient.ApiResponse delete(long id) throws IOException {
    return client.delete("/products/" + id);
  }
}
