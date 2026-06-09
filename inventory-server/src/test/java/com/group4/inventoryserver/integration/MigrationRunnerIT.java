package com.group4.inventoryserver.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MigrationRunnerIT {

  @Before
  public void setUp() {
    assumeTrue("MySQL test instance required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void migrate_creates_schema_migrations_and_users_table() throws Exception {
    new MigrationRunner().migrate();

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement("SELECT COUNT(*) AS c FROM schema_migrations");
        ResultSet rs = ps.executeQuery()) {
      rs.next();
      assertThat(rs.getInt("c"), is(greaterThan(0)));
    }

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS c FROM users");
        ResultSet rs = ps.executeQuery()) {
      rs.next();
      assertThat(rs.getInt("c"), is(greaterThan(0)));
    }
  }
}
