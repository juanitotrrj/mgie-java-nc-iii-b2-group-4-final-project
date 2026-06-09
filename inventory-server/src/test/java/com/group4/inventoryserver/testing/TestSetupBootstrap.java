package com.group4.inventoryserver.testing;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.service.SetupService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public final class TestSetupBootstrap {

  private TestSetupBootstrap() {}

  public static String mintSetupToken() {
    DatabaseConfig.initialize();
    try {
      return new SetupService().createSetupSession();
    } finally {
      DatabaseConfig.shutdown();
    }
  }

  public static void applySqlResource(String resourcePath) throws IOException {
    DatabaseConfig.initialize();
    try (InputStream is = TestSetupBootstrap.class.getResourceAsStream(resourcePath);
        Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      if (is == null) {
        throw new IOException("SQL resource not found: " + resourcePath);
      }
      String sql = readAll(is);
      for (String statement : sql.split(";")) {
        String trimmed = statement.trim();
        if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
          stmt.execute(trimmed);
        }
      }
    } catch (Exception e) {
      throw new IOException("Failed to apply SQL fixture: " + e.getMessage(), e);
    } finally {
      DatabaseConfig.shutdown();
    }
  }

  public static void markInitialized() throws IOException {
    applySqlResource("/test-sql/initialized.sql");
  }

  public static void markInfraReady() throws IOException {
    applySqlResource("/test-sql/infra_ready.sql");
  }

  private static String readAll(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
  }
}
