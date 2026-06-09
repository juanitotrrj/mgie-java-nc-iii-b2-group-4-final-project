package com.group4.inventoryserver.dto.settings;

public class DatabaseSettings {

  private String databaseType;
  private String databaseFile;
  private Boolean autoBackup;
  private Boolean backupOnExit;
  private String backupFrequency;
  private String backupTime;

  public String getDatabaseType() {
    return databaseType;
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

  public String getDatabaseFile() {
    return databaseFile;
  }

  public void setDatabaseFile(String databaseFile) {
    this.databaseFile = databaseFile;
  }

  public Boolean getAutoBackup() {
    return autoBackup;
  }

  public void setAutoBackup(Boolean autoBackup) {
    this.autoBackup = autoBackup;
  }

  public Boolean getBackupOnExit() {
    return backupOnExit;
  }

  public void setBackupOnExit(Boolean backupOnExit) {
    this.backupOnExit = backupOnExit;
  }

  public String getBackupFrequency() {
    return backupFrequency;
  }

  public void setBackupFrequency(String backupFrequency) {
    this.backupFrequency = backupFrequency;
  }

  public String getBackupTime() {
    return backupTime;
  }

  public void setBackupTime(String backupTime) {
    this.backupTime = backupTime;
  }
}
