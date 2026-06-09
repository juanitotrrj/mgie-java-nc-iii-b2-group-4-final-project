package com.group4.inventoryserver.dto.setup;

public class SetupStatusResponse {

  private String state;
  private String serverVersion;
  private boolean appSetupRequired;
  private boolean databaseReady;

  public SetupStatusResponse() {}

  public SetupStatusResponse(
      String state, String serverVersion, boolean appSetupRequired, boolean databaseReady) {
    this.state = state;
    this.serverVersion = serverVersion;
    this.appSetupRequired = appSetupRequired;
    this.databaseReady = databaseReady;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public void setServerVersion(String serverVersion) {
    this.serverVersion = serverVersion;
  }

  public boolean isAppSetupRequired() {
    return appSetupRequired;
  }

  public void setAppSetupRequired(boolean appSetupRequired) {
    this.appSetupRequired = appSetupRequired;
  }

  public boolean isDatabaseReady() {
    return databaseReady;
  }

  public void setDatabaseReady(boolean databaseReady) {
    this.databaseReady = databaseReady;
  }
}
