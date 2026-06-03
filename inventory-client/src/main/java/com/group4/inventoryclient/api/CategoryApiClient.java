package com.group4.inventoryclient.api;

import java.io.IOException;
import java.util.Map;

public class CategoryApiClient {

  private final ApiClient client;

  public CategoryApiClient(ApiClient client) {
    this.client = client;
  }

  public ApiClient.ApiResponse list(int page, String search) throws IOException {
    StringBuilder path = new StringBuilder("/categories?page=" + page);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(java.net.URLEncoder.encode(search, "UTF-8"));
    }
    return client.get(path.toString());
  }

  public ApiClient.ApiResponse get(long id) throws IOException {
    return client.get("/categories/" + id);
  }

  public ApiClient.ApiResponse create(Map<String, Object> body) throws IOException {
    return client.post("/categories", body);
  }

  public ApiClient.ApiResponse update(long id, Map<String, Object> body) throws IOException {
    return client.put("/categories/" + id, body);
  }

  public ApiClient.ApiResponse delete(long id) throws IOException {
    return client.delete("/categories/" + id);
  }
}
