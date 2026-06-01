package com.group4.inventoryserver.migration;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationRunner {

  private static final Logger log = LoggerFactory.getLogger(MigrationRunner.class);
  private static final String MIGRATIONS_PATH = "db/migrations/";
  private static final Pattern FILENAME_PATTERN = Pattern.compile("V(\\d+)__(.+)\\.sql");

  public void migrate() {
    ensureMigrationsTable();
    Set<Integer> applied = getAppliedVersions();
    List<Migration> pending = getPendingMigrations(applied);

    if (pending.isEmpty()) {
      log.info("No pending migrations.");
      return;
    }

    log.info("Found {} pending migration(s).", pending.size());
    for (Migration migration : pending) {
      applyMigration(migration);
    }
    log.info("All migrations applied successfully.");
  }

  public void rollback() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs =
            stmt.executeQuery(
                "SELECT version, description FROM schema_migrations ORDER BY version DESC LIMIT 1")) {

      if (!rs.next()) {
        log.info("Nothing to rollback.");
        return;
      }

      int version = rs.getInt("version");
      String description = rs.getString("description");
      String filename = String.format("V%03d__%s.sql", version, description);

      String downSql = loadResourceSql(MIGRATIONS_PATH + filename.replace(".sql", ".down.sql"));
      if (downSql == null || downSql.trim().isEmpty()) {
        log.error(
            "No rollback SQL found for V{}__{}. Create {}.down.sql.",
            version,
            description,
            filename.replace(".sql", ""));
        return;
      }

      log.info("Rolling back V{}__{}", version, description);
      try (Statement execStmt = conn.createStatement()) {
        for (String sql : splitStatements(downSql)) {
          execStmt.execute(sql);
        }
      }

      try (PreparedStatement ps =
          conn.prepareStatement("DELETE FROM schema_migrations WHERE version = ?")) {
        ps.setInt(1, version);
        ps.executeUpdate();
      }

      log.info("Rolled back V{}__{}.", version, description);

    } catch (SQLException e) {
      log.error("Rollback failed: {}", e.getMessage(), e);
      throw new RuntimeException("Migration rollback failed", e);
    }
  }

  public void status() {
    ensureMigrationsTable();
    Set<Integer> applied = getAppliedVersions();
    List<Migration> all = discoverMigrations();

    String separator = "------------------------------------------------------------";
    System.out.println("Migration Status:");
    System.out.println(separator);
    System.out.printf("%-8s %-35s %-10s%n", "Version", "Description", "Status");
    System.out.println(separator);

    for (Migration m : all) {
      String s = applied.contains(m.getVersion()) ? "Applied" : "Pending";
      System.out.printf("V%03d     %-35s %-10s%n", m.getVersion(), m.getDescription(), s);
    }
    System.out.println(separator);
    System.out.printf(
        "Total: %d | Applied: %d | Pending: %d%n",
        all.size(), applied.size(), all.size() - applied.size());
  }

  private void ensureMigrationsTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS schema_migrations ("
            + "version INT NOT NULL PRIMARY KEY, "
            + "description VARCHAR(255) NOT NULL, "
            + "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create schema_migrations table", e);
    }
  }

  private Set<Integer> getAppliedVersions() {
    Set<Integer> versions = new HashSet<>();
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT version FROM schema_migrations")) {
      while (rs.next()) {
        versions.add(rs.getInt("version"));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query schema_migrations", e);
    }
    return versions;
  }

  private List<Migration> getPendingMigrations(Set<Integer> applied) {
    List<Migration> all = discoverMigrations();
    List<Migration> pending = new ArrayList<>();
    for (Migration m : all) {
      if (!applied.contains(m.getVersion())) {
        pending.add(m);
      }
    }
    return pending;
  }

  private List<Migration> discoverMigrations() {
    return discoverFromIndex();
  }

  private List<Migration> discoverFromIndex() {
    List<Migration> migrations = new ArrayList<>();
    String index = loadResourceSql(MIGRATIONS_PATH + "migrations.index");
    if (index == null || index.trim().isEmpty()) {
      return migrations;
    }
    for (String line : index.split("\\n")) {
      String trimmed = line.trim();
      if (trimmed.isEmpty()) continue;
      Matcher matcher = FILENAME_PATTERN.matcher(trimmed);
      if (matcher.matches()) {
        int version = Integer.parseInt(matcher.group(1));
        String description = matcher.group(2);
        Migration m = new Migration(version, description, trimmed);
        String sql = loadResourceSql(MIGRATIONS_PATH + trimmed);
        m.setUpSql(sql);
        migrations.add(m);
      }
    }
    Collections.sort(migrations, (a, b) -> Integer.compare(a.getVersion(), b.getVersion()));
    return migrations;
  }

  private void applyMigration(Migration migration) {
    log.info("Applying V{}__{}", migration.getVersion(), migration.getDescription());

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);
      try {
        try (Statement stmt = conn.createStatement()) {
          for (String sql : splitStatements(migration.getUpSql())) {
            stmt.execute(sql);
          }
        }

        try (PreparedStatement ps =
            conn.prepareStatement(
                "INSERT INTO schema_migrations (version, description) VALUES (?, ?)")) {
          ps.setInt(1, migration.getVersion());
          ps.setString(2, migration.getDescription());
          ps.executeUpdate();
        }

        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error(
          "Migration V{}__ {} failed: {}",
          migration.getVersion(),
          migration.getDescription(),
          e.getMessage());
      throw new RuntimeException("Migration failed at V" + migration.getVersion(), e);
    }
  }

  private String loadResourceSql(String resourcePath) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      if (is == null) return null;
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      return sb.toString();
    } catch (IOException e) {
      return null;
    }
  }

  private List<String> splitStatements(String sql) {
    List<String> statements = new ArrayList<>();
    if (sql == null) return statements;
    for (String stmt : sql.split(";")) {
      String trimmed = stmt.trim();
      if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
        statements.add(trimmed);
      }
    }
    return statements;
  }
}
