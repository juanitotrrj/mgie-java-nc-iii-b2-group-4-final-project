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

public class ProductApiClientTest {

  private ApiClient apiClient;
  private ProductApiClient productClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    productClient = new ProductApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithFilters() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    productClient.list(2, "widget", "Active", "Tools");
    verify(apiClient).get(path.capture());
    String captured = path.getValue();
    assertThat(captured, containsString("/products?page=2"));
    assertThat(captured, containsString("search=widget"));
    assertThat(captured, containsString("status=Active"));
    assertThat(captured, containsString("category=Tools"));
  }

  @Test
  public void list_omitsAllFilters() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    productClient.list(1, null, "All", "All");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), is("/products?page=1"));
  }

  @Test
  public void get_create_update_delete_callCorrectPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.delete(any())).thenReturn(new ApiClient.ApiResponse(204, "{}"));

    productClient.get(10L);
    verify(apiClient).get("/products/10");

    Map<String, Object> body = new HashMap<>();
    body.put("name", "Item");
    productClient.create(body);
    verify(apiClient).post("/products", body);

    productClient.update(10L, body);
    verify(apiClient).put("/products/10", body);

    productClient.delete(10L);
    verify(apiClient).delete("/products/10");
  }
}
