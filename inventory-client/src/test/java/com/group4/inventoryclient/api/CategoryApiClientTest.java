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

public class CategoryApiClientTest {

  private ApiClient apiClient;
  private CategoryApiClient categoryClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    categoryClient = new CategoryApiClient(apiClient);
  }

  @Test
  public void list_includesSearchParam() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    categoryClient.list(1, "food");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), containsString("/categories?page=1"));
    assertThat(path.getValue(), containsString("search=food"));
  }

  @Test
  public void crud_usesCategoryPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.delete(any())).thenReturn(new ApiClient.ApiResponse(204, "{}"));

    categoryClient.get(3L);
    verify(apiClient).get("/categories/3");

    Map<String, Object> body = new HashMap<>();
    body.put("name", "Beverages");
    categoryClient.create(body);
    verify(apiClient).post("/categories", body);

    categoryClient.update(3L, body);
    verify(apiClient).put("/categories/3", body);

    categoryClient.delete(3L);
    verify(apiClient).delete("/categories/3");
  }
}
