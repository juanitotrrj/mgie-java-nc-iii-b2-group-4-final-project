package com.group4.inventoryclient.api;

import java.io.IOException;

public class AuditApiClient {

  private final ApiClient apiClient;

  public AuditApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient.ApiResponse list(
      int page, String user, String action, String module, String dateFrom, String dateTo)
      throws IOException {
    StringBuilder path = new StringBuilder("/audit-logs?page=").append(page);
    if (user != null && !user.isEmpty()) {
      path.append("&user=").append(user);
    }
    if (action != null && !action.isEmpty() && !action.equals("All")) {
      path.append("&action=").append(action);
    }
    if (module != null && !module.isEmpty() && !module.equals("All")) {
      path.append("&module=").append(module);
    }
    if (dateFrom != null && !dateFrom.isEmpty()) {
      path.append("&dateFrom=").append(dateFrom);
    }
    if (dateTo != null && !dateTo.isEmpty()) {
      path.append("&dateTo=").append(dateTo);
    }
    return apiClient.get(path.toString());
  }
}
