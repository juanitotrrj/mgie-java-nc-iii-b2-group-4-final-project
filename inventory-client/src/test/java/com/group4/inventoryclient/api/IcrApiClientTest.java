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

public class IcrApiClientTest {

  private ApiClient apiClient;
  private IcrApiClient icrClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    icrClient = new IcrApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    icrClient.list(1, "adjustment", "Pending");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), containsString("/inventory-change-requests?page=1"));
    assertThat(path.getValue(), containsString("search=adjustment"));
    assertThat(path.getValue(), containsString("status=Pending"));
  }

  @Test
  public void create_approve_reject_useCorrectPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));

    icrClient.get(11L);
    verify(apiClient).get("/inventory-change-requests/11");

    Map<String, Object> body = new HashMap<>();
    icrClient.create(body);
    verify(apiClient).post("/inventory-change-requests", body);

    icrClient.approve(11L, body);
    verify(apiClient).put("/inventory-change-requests/11/approve", body);

    icrClient.reject(11L, body);
    verify(apiClient).put("/inventory-change-requests/11/reject", body);
  }
}
