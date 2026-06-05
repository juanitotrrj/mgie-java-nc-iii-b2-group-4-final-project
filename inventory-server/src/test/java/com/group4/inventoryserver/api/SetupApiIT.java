package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryserver.ServerLauncher;
import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.E2eHttpClient;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SetupApiIT {

  private ServerLauncher launcher;
  private E2eHttpClient client;

  @Before
  public void setUp() throws Exception {
    assumeTrue(TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInfraReady();
    DatabaseConfig.initialize();

    String token = TestSetupBootstrap.mintSetupToken();
    DatabaseConfig.initialize();

    launcher = new ServerLauncher();
    launcher.start(false);
    client = new E2eHttpClient(launcher.getBaseUrl());
    client.setSetupToken(token);
  }

  @After
  public void tearDown() {
    if (launcher != null) launcher.stop();
    DatabaseConfig.shutdown();
  }

  @Test
  public void setup_status_returns_infra_ready() throws Exception {
    E2eHttpClient.HttpResult result = client.get("/setup/status");
    assertThat(result.status, is(200));
    JsonObject data = result.jsonRoot().getAsJsonObject("data");
    assertThat(data.get("state").getAsString(), is("INFRA_READY_APP_SETUP_PENDING"));
  }

  @Test
  public void setup_progress_returns_steps() throws Exception {
    E2eHttpClient.HttpResult result = client.get("/setup/progress");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("completedSteps"), is(true));
  }

  @Test
  public void roles_seed_accepts_setup_token() throws Exception {
    JsonObject body = new JsonObject();
    JsonArray roles = new JsonArray();
    roles.add("SetupTestRole");
    body.add("roles", roles);
    assertThat(client.post("/setup/roles/seed", body.toString()).status, is(200));
  }

  @Test
  public void business_settings_requires_setup_token() throws Exception {
    E2eHttpClient noToken = new E2eHttpClient(launcher.getBaseUrl());
    JsonObject body = new JsonObject();
    body.addProperty("companyName", "Test Co");
    E2eHttpClient.HttpResult result = noToken.put("/setup/business-settings", body.toString());
    assertThat(result.status, is(401));
  }
}
