package com.group4.inventoryserver.dto;

public class HealthResponse {

  private final String status;
  private final String appName;
  private final String appVersion;
  private final String appEnv;
  private final String timestamp;
  private final DatabaseStatus database;

  public HealthResponse(
      String status,
      String appName,
      String appVersion,
      String appEnv,
      String timestamp,
      DatabaseStatus database) {
    this.status = status;
    this.appName = appName;
    this.appVersion = appVersion;
    this.appEnv = appEnv;
    this.timestamp = timestamp;
    this.database = database;
  }

  public String getStatus() {
    return status;
  }

  public String getAppName() {
    return appName;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public String getAppEnv() {
    return appEnv;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public DatabaseStatus getDatabase() {
    return database;
  }

  public static class DatabaseStatus {
    private final boolean connected;
    private final long responseTimeMs;

    public DatabaseStatus(boolean connected, long responseTimeMs) {
      this.connected = connected;
      this.responseTimeMs = responseTimeMs;
    }

    public boolean isConnected() {
      return connected;
    }

    public long getResponseTimeMs() {
      return responseTimeMs;
    }
  }
}
