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
import org.junit.Before;
import org.junit.Test;

public class AuthApiClientTest {

  private ApiClient apiClient;
  private AuthApiClient authClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    authClient = new AuthApiClient(apiClient);
  }

  @Test
  public void login_returnsDataOnSuccess() throws IOException {
    String body = "{\"data\":{\"token\":\"t1\",\"user\":{\"username\":\"admin\"}}}";
    when(apiClient.post(eq("/auth/login"), any())).thenReturn(new ApiClient.ApiResponse(200, body));
    JsonObject data = authClient.login("admin", "secret");
    assertThat(data.get("token").getAsString(), is("t1"));
    verify(apiClient).post(eq("/auth/login"), any());
  }

  @Test(expected = IOException.class)
  public void login_throwsOnFailure() throws IOException {
    when(apiClient.post(eq("/auth/login"), any()))
        .thenReturn(new ApiClient.ApiResponse(401, "{\"message\":\"Bad credentials\"}"));
    authClient.login("admin", "wrong");
  }

  @Test
  public void logout_postsToLogoutEndpoint() throws IOException {
    when(apiClient.post(eq("/auth/logout"), any()))
        .thenReturn(new ApiClient.ApiResponse(200, "{}"));
    authClient.logout();
    verify(apiClient).post(eq("/auth/logout"), any());
  }

  @Test
  public void me_returnsUserDataOnSuccess() throws IOException {
    String body = "{\"data\":{\"username\":\"clerk\",\"role\":\"Clerk\"}}";
    when(apiClient.get("/auth/me")).thenReturn(new ApiClient.ApiResponse(200, body));
    JsonObject data = authClient.me();
    assertThat(data.get("username").getAsString(), is("clerk"));
  }

  @Test(expected = IOException.class)
  public void me_throwsOnFailure() throws IOException {
    when(apiClient.get("/auth/me"))
        .thenReturn(new ApiClient.ApiResponse(403, "{\"message\":\"Forbidden\"}"));
    authClient.me();
  }
}
