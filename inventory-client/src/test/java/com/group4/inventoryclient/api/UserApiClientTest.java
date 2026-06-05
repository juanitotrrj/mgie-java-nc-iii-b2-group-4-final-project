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

public class UserApiClientTest {

  private ApiClient apiClient;
  private UserApiClient userClient;

  @Before
  public void setUp() {
    apiClient = mock(ApiClient.class);
    userClient = new UserApiClient(apiClient);
  }

  @Test
  public void list_buildsQueryWithRoleAndStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    userClient.list(1, "jane", "Manager", "Active");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), containsString("/users?page=1"));
    assertThat(path.getValue(), containsString("search=jane"));
    assertThat(path.getValue(), containsString("role=Manager"));
    assertThat(path.getValue(), containsString("status=Active"));
  }

  @Test
  public void list_skipsAllRoleAndStatus() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{\"data\":[]}"));
    ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
    userClient.list(2, "", "All", "All");
    verify(apiClient).get(path.capture());
    assertThat(path.getValue(), is("/users?page=2"));
  }

  @Test
  public void crud_and_password_actions_useCorrectPaths() throws IOException {
    when(apiClient.get(any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));
    when(apiClient.post(any(), any())).thenReturn(new ApiClient.ApiResponse(201, "{}"));
    when(apiClient.put(any(), any())).thenReturn(new ApiClient.ApiResponse(200, "{}"));

    userClient.get(2L);
    verify(apiClient).get("/users/2");

    Map<String, Object> body = new HashMap<>();
    userClient.create(body);
    verify(apiClient).post("/users", body);

    userClient.update(2L, body);
    verify(apiClient).put("/users/2", body);

    userClient.deactivate(2L);
    verify(apiClient).put("/users/2/deactivate", null);

    userClient.resetPassword(2L, body);
    verify(apiClient).put("/users/2/reset-password", body);
  }
}
