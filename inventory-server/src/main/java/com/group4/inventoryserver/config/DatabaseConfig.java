package com.group4.inventoryserver.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseConfig {

  private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
  private static HikariDataSource dataSource;

  private DatabaseConfig() {}

  public static synchronized void initialize() {
    if (dataSource != null) {
      return;
    }

    HikariConfig config = new HikariConfig();
    config.setDriverClassName(EnvConfig.dbDriver());
    config.setJdbcUrl(EnvConfig.dbUrl());
    config.setUsername(EnvConfig.dbUser());
    config.setPassword(EnvConfig.dbPassword());
    config.setMinimumIdle(EnvConfig.dbPoolMinSize());
    config.setMaximumPoolSize(EnvConfig.dbPoolMaxSize());
    config.setConnectionTimeout(EnvConfig.dbPoolConnectionTimeoutMs());
    config.setIdleTimeout(EnvConfig.dbPoolIdleTimeoutMs());
    config.setMaxLifetime(EnvConfig.dbPoolMaxLifetimeMs());
    config.setPoolName("G4IMS-HikariPool");

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.setInitializationFailTimeout(-1);

    dataSource = new HikariDataSource(config);
    log.info(
        "Database connection pool initialized [url={}, pool={}..{}]",
        EnvConfig.dbUrl(),
        EnvConfig.dbPoolMinSize(),
        EnvConfig.dbPoolMaxSize());
  }

  public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
      throw new IllegalStateException(
          "Database not initialized. Call DatabaseConfig.initialize() first.");
    }
    return dataSource.getConnection();
  }

  public static HikariDataSource getDataSource() {
    return dataSource;
  }

  public static synchronized void shutdown() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      log.info("Database connection pool closed.");
      dataSource = null;
    }
  }
}
