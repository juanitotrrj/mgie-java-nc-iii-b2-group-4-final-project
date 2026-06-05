package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.Map;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class SettingsRepositoryIT {

  private SettingsRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new SettingsRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_grouped_settings() {
    Map<String, Map<String, String>> settings = repository.findAll();
    assertThat(settings, is(notNullValue()));
    assertThat(settings.isEmpty(), is(false));
  }

  @Test
  public void upsert_and_findAllByGroup_round_trip() {
    String key = "it_key_" + System.currentTimeMillis();
    repository.upsert("integration", key, "test-value", "string", 1L);
    Map<String, String> group = repository.findAllByGroup("integration");
    assertThat(group.get(key), is("test-value"));
  }
}
