package com.group4.inventoryserver.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

import com.group4.inventoryserver.ServerLauncher;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.E2eHttpClient;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerLauncherIT {

  private ServerLauncher launcher;

  @Before
  public void setUp() throws Exception {
    assumeTrue(TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    com.group4.inventoryserver.config.DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    com.group4.inventoryserver.config.DatabaseConfig.initialize();

    launcher = new ServerLauncher();
    launcher.start(false);
  }

  @After
  public void tearDown() {
    if (launcher != null) {
      launcher.stop();
    }
  }

  @Test
  public void health_returns_ok() throws Exception {
    E2eHttpClient client = new E2eHttpClient(launcher.getBaseUrl());
    E2eHttpClient.HttpResult result = client.get("/health");
    assertThat(result.status, is(200));
  }
}
