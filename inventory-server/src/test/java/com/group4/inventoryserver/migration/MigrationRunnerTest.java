package com.group4.inventoryserver.migration;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;

public class MigrationRunnerTest {

  @Test
  @SuppressWarnings("unchecked")
  public void discoverFromIndex_loadsMigrationsFromClasspath() throws Exception {
    MigrationRunner runner = new MigrationRunner();
    Method method = MigrationRunner.class.getDeclaredMethod("discoverFromIndex");
    method.setAccessible(true);

    List<Migration> migrations = (List<Migration>) method.invoke(runner);

    assertNotNull(migrations);
    assertFalse("Expected migrations from migrations.index", migrations.isEmpty());

    Migration first = migrations.get(0);
    assertTrue(first.getVersion() > 0);
    assertNotNull(first.getDescription());
    assertNotNull(first.getFilename());
    assertTrue(first.getFilename().startsWith("V"));
    assertTrue(first.getFilename().endsWith(".sql"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void discoverFromIndex_migrationsAreSortedByVersion() throws Exception {
    MigrationRunner runner = new MigrationRunner();
    Method method = MigrationRunner.class.getDeclaredMethod("discoverFromIndex");
    method.setAccessible(true);

    List<Migration> migrations = (List<Migration>) method.invoke(runner);

    for (int i = 1; i < migrations.size(); i++) {
      assertTrue(migrations.get(i).getVersion() >= migrations.get(i - 1).getVersion());
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void splitStatements_stripsCommentsAndSplits() throws Exception {
    MigrationRunner runner = new MigrationRunner();
    Method method = MigrationRunner.class.getDeclaredMethod("splitStatements", String.class);
    method.setAccessible(true);

    String sql =
        "-- comment\n"
            + "CREATE TABLE foo (id INT);\n"
            + "-- another comment\n"
            + "INSERT INTO foo VALUES (1);";

    List<String> statements = (List<String>) method.invoke(runner, sql);

    assertEquals(2, statements.size());
    assertTrue(statements.get(0).contains("CREATE TABLE foo"));
    assertTrue(statements.get(1).contains("INSERT INTO foo"));
  }
}
