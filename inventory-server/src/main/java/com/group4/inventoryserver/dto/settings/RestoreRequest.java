package com.group4.inventoryserver.dto.settings;

public class RestoreRequest {

  private String backupFile;

  public String getBackupFile() {
    return backupFile;
  }

  public void setBackupFile(String backupFile) {
    this.backupFile = backupFile;
  }
}
