package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class SystemInstallationRepositoryIT {

  private SystemInstallationRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new SystemInstallationRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void getSetupState_returns_initialized_after_bootstrap() {
    assertThat(repository.getSetupState(), is("INITIALIZED"));
    assertThat(repository.getServerVersion(), is(notNullValue()));
    assertThat(repository.getInitializedAt(), is(notNullValue()));
  }

  @Test
  public void updateState_persists_new_value() {
    repository.updateState("TEST_STATE");
    assertThat(repository.getSetupState(), is("TEST_STATE"));
    repository.markInitialized();
    assertThat(repository.getSetupState(), is("INITIALIZED"));
  }
}
