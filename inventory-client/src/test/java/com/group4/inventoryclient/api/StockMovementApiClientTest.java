package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class StockMovementApiClientTest {

  private ApiClient apiClient;
  private StockMovementApiClient stockClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    stockClient = new StockMovementApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithFilters() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    stockClient.list(1, "42", "IN", "2024-01-01", "2024-01-31");
    verify(apiClient).get(path.capture());
    String captured = path.getValue();
    assertThat(captured, containsString("/stock-movements?page=1"));
    assertThat(captured, containsString("productId=42"));
    assertThat(captured, containsString("movementType=IN"));
    assertThat(captured, containsString("dateFrom=2024-01-01"));
    assertThat(captured, containsString("dateTo=2024-01-31"));
  }

  @Test
  public void list_omitsAllMovementType() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    stockClient.list(1, null, "all", null, null);
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), is("/stock-movements?page=1"));
  }
}
