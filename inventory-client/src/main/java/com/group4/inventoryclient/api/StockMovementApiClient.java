package com.group4.inventoryclient.api;

import java.io.IOException;

public class StockMovementApiClient {

  private final ApiClient apiClient;

  public StockMovementApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse list(
      int page, String productId, String movementType, String dateFrom, String dateTo)
      throws IOException {
    StringBuilder path = new StringBuilder("/stock-movements?page=" + page);
    if (productId != null && !productId.isEmpty()) {
      path.append("&productId=").append(urlEncode(productId));
    }
    if (movementType != null && !movementType.isEmpty() && !movementType.equalsIgnoreCase("All")) {
      path.append("&movementType=").append(urlEncode(movementType));
    }
    if (dateFrom != null && !dateFrom.isEmpty()) {
      path.append("&dateFrom=").append(urlEncode(dateFrom));
    }
    if (dateTo != null && !dateTo.isEmpty()) {
      path.append("&dateTo=").append(urlEncode(dateTo));
    }
    return apiClient.get(path.toString());
  }

  private String urlEncode(String value) {
    try {
      return java.net.URLEncoder.encode(value, "UTF-8");
    } catch (Exception e) {
      return value;
    }
  }
}
