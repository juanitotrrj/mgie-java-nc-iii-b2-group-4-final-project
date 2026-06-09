package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.audit.AuditLogData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class AuditLogRepositoryIT {

  private AuditLogRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new AuditLogRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void insert_and_findAll_round_trip() {
    repository.insert(
        1L, "admin", "CREATE", "Products", "P001", "Integration test audit entry", "127.0.0.1");
    List<AuditLogData> logs =
        repository.findAll(
            0, 10, "createdAt", "desc", "Integration test", null, null, null, null, null);
    assertThat(logs.size(), is(greaterThan(0)));
    assertThat(
        repository.count("Integration test", null, null, null, null, null), is(greaterThan(0L)));
  }
}
