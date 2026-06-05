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

public class AuditApiClientTest {

  private ApiClient apiClient;
  private AuditApiClient auditClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    auditClient = new AuditApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithFilters() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    auditClient.list(1, "admin", "CREATE", "products", "2024-01-01", "2024-01-31");
    verify(apiClient).get(path.capture());
    String captured = path.getValue();
    assertThat(captured, containsString("/audit-logs?page=1"));
    assertThat(captured, containsString("user=admin"));
    assertThat(captured, containsString("action=CREATE"));
    assertThat(captured, containsString("module=products"));
    assertThat(captured, containsString("dateFrom=2024-01-01"));
    assertThat(captured, containsString("dateTo=2024-01-31"));
  }

  @Test
  public void list_skipsAllActionAndModule() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    auditClient.list(2, "", "All", "All", "", "");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), is("/audit-logs?page=2"));
  }
}
