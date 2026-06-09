package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.sale.SaleData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.sql.Connection;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class SaleRepositoryIT {

  private SaleRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new SaleRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_seeded_sales() {
    List<SaleData> sales =
        repository.findAll(0, 10, "createdAt", "desc", null, null, null, null, null, null);
    assertThat(sales.size(), is(greaterThan(0)));
    assertThat(repository.count(null, null, null, null, null, null), is(greaterThan(0L)));
  }

  @Test
  public void insert_findById_and_findItems_round_trip() throws Exception {
    long productId =
        new ProductRepository().findAll(0, 1, null, null, null, null, null).get(0).getProductId();
    String invoiceNo = repository.generateNextInvoiceNo();
    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);
      long saleId =
          repository.insert(
              conn,
              invoiceNo,
              "IT Customer",
              1L,
              "Cash",
              "Completed",
              100.0,
              0.0,
              100.0,
              100.0,
              0.0,
              1L);
      repository.insertItem(conn, saleId, productId, 1, 100.0, 100.0);
      conn.commit();

      SaleData found = repository.findById(saleId);
      assertThat(found, is(notNullValue()));
      assertThat(found.getInvoiceNo(), is(invoiceNo));
      assertThat(repository.findItemsBySaleId(saleId).size(), is(1));
    }
  }
}
