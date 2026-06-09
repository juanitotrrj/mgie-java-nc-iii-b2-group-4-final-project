package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.product.StockMovementData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class StockMovementRepositoryIT {

  private StockMovementRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new StockMovementRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_seeded_movements() {
    List<StockMovementData> movements =
        repository.findAll(0, 10, "createdAt", "desc", null, null, null, null, null, null);
    assertThat(movements.size(), is(greaterThan(0)));
    assertThat(repository.count(null, null, null, null, null, null), is(greaterThan(0L)));
  }

  @Test
  public void create_increases_movement_count() {
    long productId =
        new ProductRepository().findAll(0, 1, null, null, null, null, null).get(0).getProductId();
    long before = repository.count(null, null, null, null, null, null);
    repository.create(
        productId, "ADJUSTMENT", "ICR", 1L, "ICR-IT-001", 10, 1, 11, "Integration test", 1L);
    assertThat(repository.count(null, null, null, null, null, null), is(before + 1));
  }
}
