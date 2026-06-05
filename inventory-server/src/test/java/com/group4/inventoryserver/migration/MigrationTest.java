package com.group4.inventoryserver.migration;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import org.junit.Test;

public class MigrationTest {

  @Test
  public void constructor_setsVersionDescriptionFilename() {
    Migration migration = new Migration(1, "create_users_table", "V001__create_users_table.sql");

    assertEquals(1, migration.getVersion());
    assertEquals("create_users_table", migration.getDescription());
    assertEquals("V001__create_users_table.sql", migration.getFilename());
  }

  @Test
  public void setters_storeSqlAndAppliedAt() {
    Migration migration = new Migration(5, "add_index", "V005__add_index.sql");
    LocalDateTime appliedAt = LocalDateTime.of(2026, 1, 15, 10, 30);

    migration.setUpSql("CREATE INDEX idx_name ON users(name);");
    migration.setDownSql("DROP INDEX idx_name ON users;");
    migration.setAppliedAt(appliedAt);

    assertEquals("CREATE INDEX idx_name ON users(name);", migration.getUpSql());
    assertEquals("DROP INDEX idx_name ON users;", migration.getDownSql());
    assertEquals(appliedAt, migration.getAppliedAt());
  }
}
