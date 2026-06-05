package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.purchase.PurchaseData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.time.LocalDate;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class PurchaseRepositoryIT {

  private PurchaseRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new PurchaseRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_seeded_purchase_orders() {
    List<PurchaseData> orders =
        repository.findAll(0, 10, "createdAt", "desc", null, null, null, null, null);
    assertThat(orders.size(), is(greaterThan(0)));
    assertThat(repository.count(null, null, null, null, null), is(greaterThan(0L)));
  }

  @Test
  public void insert_findById_and_findItems_round_trip() {
    long supplierId =
        new SupplierRepository().findAll(0, 1, null, null, null, null, null).get(0).getSupplierId();
    long productId =
        new ProductRepository().findAll(0, 1, null, null, null, null, null).get(0).getProductId();
    String today = LocalDate.now().toString();
    String poNumber = repository.generateNextPoNumber();
    long id = repository.insert(poNumber, supplierId, today, today, "IT PO", 100.0, 1L);
    assertThat(id, is(greaterThan(0L)));

    repository.insertItem(id, productId, 2, 50.0, 100.0);

    PurchaseData found = repository.findById(id);
    assertThat(found, is(notNullValue()));
    assertThat(found.getPoNumber(), is(poNumber));
    assertThat(repository.findItemsByPurchaseId(id).size(), is(1));
  }
}
