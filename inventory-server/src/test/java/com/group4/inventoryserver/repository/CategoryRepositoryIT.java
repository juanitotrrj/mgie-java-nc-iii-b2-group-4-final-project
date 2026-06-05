package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.category.CategoryData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.List;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class CategoryRepositoryIT {

  private CategoryRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new CategoryRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findAll_returns_seeded_categories() {
    List<CategoryData> categories =
        repository.findAll(0, 10, "categoryName", "asc", null, null, null);
    assertThat(categories.size(), is(greaterThan(0)));
    assertThat(repository.count(null, null, null), is(greaterThan(0L)));
  }

  @Test
  public void insert_findById_and_update_round_trip() {
    String code = repository.generateNextCode();
    String name = "IT Category " + System.currentTimeMillis();
    long id = repository.insert(code, name, "Integration test", "Product Group", "Active", 1L);
    assertThat(id, is(greaterThan(0L)));

    CategoryData found = repository.findById(id);
    assertThat(found, is(notNullValue()));
    assertThat(found.getCategoryName(), is(name));

    repository.update(id, name + " Updated", "Updated desc", null, null, 1L);
    assertThat(repository.findById(id).getCategoryName(), is(name + " Updated"));
  }
}
