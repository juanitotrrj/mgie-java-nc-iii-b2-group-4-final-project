package com.group4.inventoryclient.api;

import com.google.gson.JsonObject;
import java.io.IOException;

public class DashboardApiClient {

  private final ApiClient client;

  public DashboardApiClient(ApiClient client) {
    this.client = client;
  }

  public JsonObject getAdminDashboard() throws IOException {
    ApiClient.ApiResponse r = client.get("/dashboard/admin");
    if (!r.isSuccess()) throw new IOException(r.getErrorMessage());
    return r.getDataAsObject();
  }

  public JsonObject getManagerDashboard() throws IOException {
    ApiClient.ApiResponse r = client.get("/dashboard/manager");
    if (!r.isSuccess()) throw new IOException(r.getErrorMessage());
    return r.getDataAsObject();
  }

  public JsonObject getClerkDashboard() throws IOException {
    ApiClient.ApiResponse r = client.get("/dashboard/clerk");
    if (!r.isSuccess()) throw new IOException(r.getErrorMessage());
    return r.getDataAsObject();
  }

  public JsonObject getCashierDashboard() throws IOException {
    ApiClient.ApiResponse r = client.get("/dashboard/cashier");
    if (!r.isSuccess()) throw new IOException(r.getErrorMessage());
    return r.getDataAsObject();
  }
}
