package com.group4.inventoryserver.dto.settings;

public class DatabaseTestResult {

  private final String status;
  private final String databaseType;
  private final String databaseName;
  private final long responseTimeMs;

  public DatabaseTestResult(
      String status, String databaseType, String databaseName, long responseTimeMs) {
    this.status = status;
    this.databaseType = databaseType;
    this.databaseName = databaseName;
    this.responseTimeMs = responseTimeMs;
  }

  public String getStatus() {
    return status;
  }

  public String getDatabaseType() {
    return databaseType;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public long getResponseTimeMs() {
    return responseTimeMs;
  }
}
