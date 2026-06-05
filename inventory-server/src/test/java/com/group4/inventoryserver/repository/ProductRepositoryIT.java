package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.product.ProductData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class ProductRepositoryIT {

  private ProductRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new ProductRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_seeded_products() {
    List<ProductData> products = repository.findAll(0, 10, "productName", "asc", null, null, null);
    assertThat(products.size(), is(greaterThan(0)));
    assertThat(repository.count(null, null, null), is(greaterThan(0L)));
  }

  @Test
  public void insert_findById_and_update_round_trip() {
    long categoryId =
        new CategoryRepository().findAll(0, 1, null, null, null, null, null).get(0).getCategoryId();
    String code = "ITP" + System.currentTimeMillis();
    long id = repository.insert(code, "IT Product", categoryId, null, 5, 2, 99.99, "In Stock", 1L);
    assertThat(id, is(greaterThan(0L)));

    ProductData found = repository.findById(id);
    assertThat(found, is(notNullValue()));
    assertThat(found.getProductCode(), is(code));

    repository.update(id, "IT Product Updated", null, null, null, null, null, 1L);
    assertThat(repository.findById(id).getProductName(), is("IT Product Updated"));
  }
}
