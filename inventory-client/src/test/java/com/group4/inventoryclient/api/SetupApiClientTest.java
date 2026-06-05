package com.group4.inventoryclient.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class SetupApiClientTest {

  private ApiClient apiClient;
  private SetupApiClient setupClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    setupClient = new SetupApiClient(apiClient);
  }

  @Test
  public void getStatus_returnsData() throws IOException {
    when(apiClient.get("/setup/status"))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"installed\":false}}"));
    JsonObject data = setupClient.getStatus();
    assertThat(data.get("installed").getAsBoolean(), is(false));
  }

  @Test
  public void rootLogin_setsSetupToken() throws IOException {
    when(apiClient.post(eq("/setup/root-login"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"token\":\"setup-jwt\"}}"));
    String token = setupClient.rootLogin("root", "pass");
    assertThat(token, is("setup-jwt"));
    verify(apiClient).setSetupToken("setup-jwt");
  }

  @Test(expected = IOException.class)
  public void rootLogin_throwsOnFailure() throws IOException {
    when(apiClient.post(eq("/setup/root-login"), any()))
        .thenReturn(new ApiClient.ApiResponse(401, "{\"message\":\"Denied\"}"));
    setupClient.rootLogin("root", "bad");
  }

  @Test
  public void saveBusinessSettings_putsSettings() throws IOException {
    when(apiClient.put(eq("/setup/business-settings"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{}"));
    Map<String, Object> settings = new HashMap<>();
    settings.put("businessName", "G4");
    setupClient.saveBusinessSettings(settings);
    verify(apiClient).put("/setup/business-settings", settings);
  }

  @Test
  public void seedRoles_postsRoles() throws IOException {
    when(apiClient.post(eq("/setup/roles/seed"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{}"));
    setupClient.seedRoles(Arrays.asList("Admin", "Clerk"));
    verify(apiClient).post(eq("/setup/roles/seed"), any());
  }

  @Test
  public void seedPermissions_postsRolePermissions() throws IOException {
    when(apiClient.post(eq("/setup/permissions/seed"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{}"));
    Map<String, List<String>> perms = new HashMap<>();
    perms.put("Admin", Collections.singletonList("USER_MANAGE"));
    setupClient.seedPermissions(perms);
    verify(apiClient).post(eq("/setup/permissions/seed"), any());
  }

  @Test
  public void createUsers_postsUsers() throws IOException {
    when(apiClient.post(eq("/setup/users"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{}"));
    Map<String, String> user = new HashMap<>();
    user.put("username", "admin");
    setupClient.createUsers(Collections.singletonList(user));
    verify(apiClient).post(eq("/setup/users"), any());
  }

  @Test
  public void finish_returnsData() throws IOException {
    when(apiClient.post(eq("/setup/finish"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"completed\":true}}"));
    JsonObject data = setupClient.finish();
    assertThat(data.get("completed").getAsBoolean(), is(true));
  }

  @Test
  public void getProgress_returnsData() throws IOException {
    when(apiClient.get("/setup/progress"))
        .thenReturn(new ApiClient.ApiResponse(200, "{\"data\":{\"step\":3}}"));
    JsonObject data = setupClient.getProgress();
    assertThat(data.get("step").getAsInt(), is(3));
  }
}
