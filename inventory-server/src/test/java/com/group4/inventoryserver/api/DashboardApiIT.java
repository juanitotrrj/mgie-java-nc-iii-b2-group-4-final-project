package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class DashboardApiIT extends ApiITBase {

  @Test
  public void admin_dashboard_returns_metrics() throws Exception {
    E2eHttpClient.HttpResult result = adminClient.get("/dashboard/admin");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("metrics"), is(true));
  }
}
