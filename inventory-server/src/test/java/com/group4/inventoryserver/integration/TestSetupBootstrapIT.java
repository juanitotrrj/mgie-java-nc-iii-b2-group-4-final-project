package com.group4.inventoryserver.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assume.assumeTrue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSetupBootstrapIT {

  @Before
  public void setUp() throws Exception {
    assumeTrue(TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void mintSetupToken_returns_non_empty_uuid() {
    String token = TestSetupBootstrap.mintSetupToken();
    assertThat(token, is(not(nullValue())));
    assertThat(token.isEmpty(), is(false));
  }

  @Test
  public void markInitialized_sets_setup_state() throws Exception {
    TestSetupBootstrap.markInitialized();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT setup_state FROM system_installation WHERE installation_id = 1");
        ResultSet rs = ps.executeQuery()) {
      rs.next();
      assertThat(rs.getString("setup_state"), is("INITIALIZED"));
    }
  }
}
