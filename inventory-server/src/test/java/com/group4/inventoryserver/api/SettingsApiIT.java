package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class SettingsApiIT extends ApiITBase {

  @Test
  public void get_settings_returns_grouped_configuration() throws Exception {
    E2eHttpClient.HttpResult result = adminClient.get("/settings");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").size() > 0, is(true));
  }
}
