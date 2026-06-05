package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class DashboardApiClientTest {

  private ApiClient apiClient;
  private DashboardApiClient dashboardClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    dashboardClient = new DashboardApiClient(apiClient);
  }

  @Test
  public void getAdminDashboard_returnsData() throws IOException {
    when(apiClient.get("/dashboard/admin"))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"totalProducts\":5}}"));
    JsonObject data = dashboardClient.getAdminDashboard();
    assertThat(data.get("totalProducts").getAsInt(), is(5));
    verify(apiClient).get("/dashboard/admin");
  }

  @Test
  public void getManagerDashboard_returnsData() throws IOException {
    when(apiClient.get("/dashboard/manager"))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"pendingOrders\":2}}"));
    JsonObject data = dashboardClient.getManagerDashboard();
    assertThat(data.get("pendingOrders").getAsInt(), is(2));
  }

  @Test
  public void getClerkDashboard_returnsData() throws IOException {
    when(apiClient.get("/dashboard/clerk"))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"lowStockCount\":1}}"));
    JsonObject data = dashboardClient.getClerkDashboard();
    assertThat(data.get("lowStockCount").getAsInt(), is(1));
  }

  @Test
  public void getCashierDashboard_returnsData() throws IOException {
    when(apiClient.get("/dashboard/cashier"))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"todaySales\":100}}"));
    JsonObject data = dashboardClient.getCashierDashboard();
    assertThat(data.get("todaySales").getAsInt(), is(100));
  }

  @Test(expected = IOException.class)
  public void getAdminDashboard_throwsOnFailure() throws IOException {
    when(apiClient.get("/dashboard/admin"))
        .thenReturn(new ApiClient.ApiResponse(500, "{\"message\":\"Unavailable\"}"));
    dashboardClient.getAdminDashboard();
  }
}
