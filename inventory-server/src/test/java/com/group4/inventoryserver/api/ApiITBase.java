package com.group4.inventoryserver.api;

import com.group4.inventoryserver.ServerLauncher;
import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.E2eHttpClient;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

public abstract class ApiITBase {

  protected ServerLauncher launcher;
  protected E2eHttpClient client;
  protected E2eHttpClient adminClient;

  @Before
  public void baseSetUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    DatabaseConfig.initialize();
    launcher = new ServerLauncher();
    launcher.start(false);
    client = new E2eHttpClient(launcher.getBaseUrl());
    adminClient = new E2eHttpClient(launcher.getBaseUrl());
    adminClient.login("admin", "Admin@123");
  }

  @After
  public void baseTearDown() {
    if (launcher != null) {
      launcher.stop();
    }
    DatabaseConfig.shutdown();
  }
}
