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

public class DatabaseBackupRepositoryIT {

  private DatabaseBackupRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new DatabaseBackupRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void insert_and_findPathByFilename_round_trip() {
    String filename = "it-backup-" + System.currentTimeMillis() + ".sql";
    String path = "/tmp/" + filename;
    long id = repository.insert(filename, path, 1024L, "Created", 1L);
    assertThat(id, is(greaterThan(0L)));
    assertThat(repository.findPathByFilename(filename), is(path));
  }
}
