package com.group4.inventoryserver.dto.settings;

public class BackupResult {

  private final String backupFile;
  private final String createdAt;

  public BackupResult(String backupFile, String createdAt) {
    this.backupFile = backupFile;
    this.createdAt = createdAt;
  }

  public String getBackupFile() {
    return backupFile;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
