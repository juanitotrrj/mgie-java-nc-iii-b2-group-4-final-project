package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SaleApiClientTest {

  private ApiClient apiClient;
  private SaleApiClient saleClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    saleClient = new SaleApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    saleClient.list(1, "receipt", "Paid");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), containsString("/sales?page=1"));
    assertThat(path.getValue(), containsString("search=receipt"));
    assertThat(path.getValue(), containsString("status=Paid"));
  }

  @Test
  public void get_create_cancel_useCorrectPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));

    saleClient.get(9L);
    verify(apiClient).get("/sales/9");

    Map<String, Object> body = new HashMap<>();
    saleClient.create(body);
    verify(apiClient).post("/sales", body);

    saleClient.cancel(9L, body);
    verify(apiClient).post("/sales/9/cancel", body);
  }
}
