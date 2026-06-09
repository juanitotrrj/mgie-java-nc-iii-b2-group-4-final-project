package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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

public class PurchaseApiClientTest {

  private ApiClient apiClient;
  private PurchaseApiClient purchaseClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    purchaseClient = new PurchaseApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    purchaseClient.list(1, "PO-1", "Pending");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), containsString("/purchases?page=1"));
    assertThat(path.getValue(), containsString("search=PO-1"));
    assertThat(path.getValue(), containsString("status=Pending"));
  }

  @Test
  public void list_ignoresAllStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    purchaseClient.list(1, null, "all");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), is("/purchases?page=1"));
  }

  @Test
  public void get_create_receive_cancel_useCorrectPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));

    purchaseClient.get(4L);
    verify(apiClient).get("/purchases/4");

    Map<String, Object> body = new HashMap<>();
    purchaseClient.create(body);
    verify(apiClient).post("/purchases", body);

    purchaseClient.receive(4L, body);
    verify(apiClient).post("/purchases/4/receive", body);

    purchaseClient.cancel(4L, body);
    verify(apiClient).post("/purchases/4/cancel", body);
  }
}
