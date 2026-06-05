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

public class SupplierApiClientTest {

  private ApiClient apiClient;
  private SupplierApiClient supplierClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    supplierClient = new SupplierApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    supplierClient.list(1, "acme", "Active");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), containsString("/suppliers?page=1"));
    assertThat(path.getValue(), containsString("search=acme"));
    assertThat(path.getValue(), containsString("status=Active"));
  }

  @Test
  public void list_skipsAllStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    supplierClient.list(1, "", "All");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), is("/suppliers?page=1"));
  }

  @Test
  public void crud_usesSupplierPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.delete(any())).thenReturn(new ApiClient.ApiResponse(204, "{}"));

    supplierClient.get(7L);
    verify(apiClient).get("/suppliers/7");

    Map<String, Object> body = new HashMap<>();
    supplierClient.create(body);
    verify(apiClient).post("/suppliers", body);

    supplierClient.update(7L, body);
    verify(apiClient).put("/suppliers/7", body);

    supplierClient.delete(7L);
    verify(apiClient).delete("/suppliers/7");
  }
}
