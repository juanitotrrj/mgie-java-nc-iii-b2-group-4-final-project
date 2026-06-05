package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class SessionRepositoryIT {

  private SessionRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new SessionRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void create_findActiveByTokenHash_and_invalidate_round_trip() {
    String tokenHash = "it-session-hash-" + System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 1);
    Timestamp expiresAt = new Timestamp(cal.getTimeInMillis());

    repository.create(1L, tokenHash, "127.0.0.1", "IntegrationTest/1.0", expiresAt);
    Map<String, Object> session = repository.findActiveByTokenHash(tokenHash);
    assertThat(session, is(notNullValue()));
    assertThat(session.get("userId"), is(1L));

    repository.invalidate((Long) session.get("sessionId"), "LoggedOut");
    assertThat(repository.findActiveByTokenHash(tokenHash), is(nullValue()));
  }
}
