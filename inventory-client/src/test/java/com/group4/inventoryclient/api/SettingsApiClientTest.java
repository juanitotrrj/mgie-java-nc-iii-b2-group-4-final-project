package com.group4.inventoryclient.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class SettingsApiClientTest {

  private ApiClient apiClient;
  private SettingsApiClient settingsClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    settingsClient = new SettingsApiClient(apiClient);
  }

  @Test
  public void get_usesSectionQueryParam() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    settingsClient.get("business");
    verify(apiClient).get("/settings?section=business");
  }

  @Test
  public void update_putsToSectionPath() throws IOException {
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    Map<String, Object> body = new HashMap<>();
    body.put("name", "G4 Store");
    settingsClient.update("business", body);
    verify(apiClient).put("/settings/business", body);
  }

  @Test
  public void testConnection_and_backup_useCorrectPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));

    settingsClient.testConnection();
    verify(apiClient).get("/settings/test-connection");

    settingsClient.backup();
    verify(apiClient).post("/settings/backup", null);
  }
}
