package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class DashboardRepositoryIT {

  private DashboardRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new DashboardRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void countActiveProducts_returns_seeded_count() {
    assertThat(repository.countActiveProducts(), is(greaterThan(0)));
  }

  @Test
  public void countActiveUsers_returns_at_least_admin() {
    assertThat(repository.countActiveUsers(), is(greaterThan(0)));
  }
}
