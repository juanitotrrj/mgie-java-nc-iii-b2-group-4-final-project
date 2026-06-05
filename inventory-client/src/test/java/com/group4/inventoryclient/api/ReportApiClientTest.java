package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ReportApiClientTest {

  private ApiClient apiClient;
  private ReportApiClient reportClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    reportClient = new ReportApiClient(apiClient);
  }

  @Test
  public void generate_buildsQueryString() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{}}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    reportClient.generate("sales", "2024-01-01", "2024-01-31", "Food");
    verify(apiClient).get(path.capture());
    String captured = path.getValue();
    assertThat(captured, containsString("/reports?"));
    assertThat(captured, containsString("type=sales"));
    assertThat(captured, containsString("dateFrom=2024-01-01"));
    assertThat(captured, containsString("dateTo=2024-01-31"));
    assertThat(captured, containsString("category=Food"));
  }
}
