package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.icr.IcrData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class IcrRepositoryIT {

  private IcrRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new IcrRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_seeded_icrs() {
    List<IcrData> icrs =
        repository.findAll(0, 10, "requestedAt", "desc", null, null, null, null, null, null);
    assertThat(icrs.size(), is(greaterThan(0)));
    assertThat(repository.count(null, null, null, null, null, null), is(greaterThan(0L)));
  }

  @Test
  public void insert_and_findById_round_trip() {
    long productId =
        new ProductRepository().findAll(0, 1, null, null, null, null, null).get(0).getProductId();
    String requestNo = repository.generateNextRequestNo();
    long id =
        repository.insert(
            requestNo, productId, "Adjustment", 10, 12, 2, "Integration test adjustment", 1L);
    assertThat(id, is(greaterThan(0L)));

    IcrData found = repository.findById(id);
    assertThat(found, is(notNullValue()));
    assertThat(found.getRequestNo(), is(requestNo));
  }
}
