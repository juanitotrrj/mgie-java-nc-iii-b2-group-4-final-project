package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.HealthResponse;
import com.group4.inventoryserver.dto.HealthResponse.DatabaseStatus;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.util.DateUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

public class HealthHandler extends BaseHandler {

  @Override
  protected void handleGet(RequestContext ctx) throws IOException {
    DatabaseStatus dbStatus = checkDatabase();

    String status = dbStatus.isConnected() ? "healthy" : "degraded";

    HealthResponse health =
        new HealthResponse(
            status,
            EnvConfig.appName(),
            EnvConfig.appVersion(),
            EnvConfig.appEnv(),
            DateUtil.nowIso(),
            dbStatus);

    sendSuccess(ctx, health);
  }

  private DatabaseStatus checkDatabase() {
    long start = System.currentTimeMillis();
    try {
      if (DatabaseConfig.getDataSource() == null || DatabaseConfig.getDataSource().isClosed()) {
        return new DatabaseStatus(false, System.currentTimeMillis() - start);
      }
      try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
        if (!conn.isValid(5)) {
          return new DatabaseStatus(false, System.currentTimeMillis() - start);
        }
        try (Statement stmt = conn.createStatement()) {
          stmt.setQueryTimeout(5);
          stmt.execute("SELECT 1");
        }
        long elapsed = System.currentTimeMillis() - start;
        return new DatabaseStatus(true, elapsed);
      }
    } catch (Exception e) {
      long elapsed = System.currentTimeMillis() - start;
      return new DatabaseStatus(false, elapsed);
    }
  }
}
