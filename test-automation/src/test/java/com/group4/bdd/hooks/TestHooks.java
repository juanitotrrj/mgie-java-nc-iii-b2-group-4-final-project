package com.group4.bdd.hooks;

import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.inventoryserver.ServerLauncher;
import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.E2eHttpClient;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.junit.Assume;

public class TestHooks {

  private final TestContext context = SharedTestContext.get();

  @Before
  public void beforeScenario(Scenario scenario) {
    if (scenario.getSourceTagNames().contains("@root-login")
        || scenario.getSourceTagNames().contains("@deferred")) {
      Assume.assumeTrue("Deferred root-login scenarios are excluded", false);
    }
    Assume.assumeTrue("MySQL test DB required", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
  }

  @Before("@infra_ready")
  public void useInfraReadyFixture() {
    context.setFixture("infra_ready");
  }

  @Before("@api or @e2e or @uat")
  public void prepareDatabase() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    try {
      TestEnv.loadTestEnv();
      DatabaseConfig.initialize();
      new MigrationRunner().migrate();
      if ("infra_ready".equals(context.getFixture())) {
        TestSetupBootstrap.markInfraReady();
      } else {
        TestSetupBootstrap.markInitialized();
      }
      DatabaseConfig.initialize();
    } catch (Exception e) {
      Assume.assumeNoException("MySQL test DB migration failed — start docker-compose.test.yml", e);
    }
  }

  @After("@api or @e2e or @uat")
  public void stopServer() {
    ServerLauncher launcher = context.getServerLauncher();
    if (launcher != null) {
      launcher.stop();
      context.setServerLauncher(null);
    }
    DatabaseConfig.shutdown();
    SharedTestContext.reset();
  }
}
