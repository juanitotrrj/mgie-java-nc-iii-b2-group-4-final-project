package com.group4.inventoryserver.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.user.UserData;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.security.PasswordUtil;
import com.group4.inventoryserver.testing.TestEnv;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class UserRepositoryIT {

  private UserRepository repository;

  @Before
  public void setUp() throws Exception {
    Assume.assumeTrue("MySQL test DB required on port 3307", TestEnv.isMysqlAvailable());
    TestEnv.loadTestEnv();
    DatabaseConfig.initialize();
    new MigrationRunner().migrate();
    TestSetupBootstrap.markInitialized();
    repository = new UserRepository();
  }

  @After
  public void tearDown() {
    DatabaseConfig.shutdown();
  }

  @Test
  public void findByUsername_returns_admin_user() {
    Map<String, Object> admin = repository.findByUsername("admin");
    assertThat(admin, is(notNullValue()));
    assertThat(admin.get("username"), is("admin"));
    assertThat(
        repository.findPermissionsByRoleId((Long) admin.get("roleId")).size(), is(greaterThan(0)));
  }

  @Test
  public void insert_findDetailById_and_update_round_trip() {
    Long roleId = repository.getRoleIdByName("Cashier");
    assertThat(roleId, is(notNullValue()));
    String code = repository.generateNextUserCode();
    String username = "ituser" + System.currentTimeMillis();
    long id =
        repository.insert(
            code,
            "IT User",
            username,
            username + "@test.local",
            PasswordUtil.hash("Test@1234"),
            roleId,
            "Active",
            1L);
    assertThat(id, is(greaterThan(0L)));

    UserData found = repository.findDetailById(id);
    assertThat(found.getUsername(), is(username));

    repository.update(id, "IT User Updated", username + "@updated.local", roleId, "Active", 1L);
    assertThat(repository.findDetailById(id).getFullName(), is("IT User Updated"));

    List<UserData> users = repository.findAll(0, 10, null, null, username, null, null);
    assertThat(users.size(), is(1));
  }
}
