package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.sql.Timestamp;
import java.util.Calendar;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class SetupSessionRepositoryIT {

  private SetupSessionRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new SetupSessionRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void create_isValid_and_finish_round_trip() {
    String tokenHash = "it-setup-hash-" + System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 1);
    Timestamp expiresAt = new Timestamp(cal.getTimeInMillis());

    repository.create(tokenHash, expiresAt);
    assertThat(repository.isValid(tokenHash), is(true));

    repository.finish(tokenHash);
    assertThat(repository.isValid(tokenHash), is(false));
  }
}
