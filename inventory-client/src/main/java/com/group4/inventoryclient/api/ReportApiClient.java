package com.group4.inventoryclient.api;

import java.io.IOException;

public class ReportApiClient {

  private final ApiClient apiClient;

  public ReportApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse generate(
      String type, String dateFrom, String dateTo, String category) throws IOException {
    StringBuilder path = new StringBuilder("/reports?");
    if (type != null && !type.isEmpty()) {
      path.append("type=").append(type);
    }
    if (dateFrom != null && !dateFrom.isEmpty()) {
      path.append("&dateFrom=").append(dateFrom);
    }
    if (dateTo != null && !dateTo.isEmpty()) {
      path.append("&dateTo=").append(dateTo);
    }
    if (category != null && !category.isEmpty()) {
      path.append("&category=").append(category);
    }
    return apiClient.get(path.toString());
  }
}
