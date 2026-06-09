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
  public void getAll_usesSettingsRootPath() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    settingsClient.getAll();
    verify(apiClient).get("/settings");
  }

  @Test
  public void updateCompany_putsToCompanyPath() throws IOException {
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    Map<String, Object> body = new HashMap<>();
    body.put("companyName", "G4 Store");
    settingsClient.updateCompany(body);
    verify(apiClient).put("/settings/company", body);
  }

  @Test
  public void testConnection_and_backup_useDatabasePaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));

    settingsClient.testConnection();
    verify(apiClient).get("/settings/database/test");

    settingsClient.backup();
    verify(apiClient).post("/settings/database/backup", null);
  }
}
