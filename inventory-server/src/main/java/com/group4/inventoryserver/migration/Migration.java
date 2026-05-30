package com.group4.inventoryserver.migration;

import java.time.LocalDateTime;

public class Migration {

  private final int version;
  private final String description;
  private final String filename;
  private String upSql;
  private String downSql;
  private LocalDateTime appliedAt;

  public Migration(int version, String description, String filename) {
    this.version = version;
    this.description = description;
    this.filename = filename;
  }

  public int getVersion() {
    return version;
  }

  public String getDescription() {
    return description;
  }

  public String getFilename() {
    return filename;
  }

  public String getUpSql() {
    return upSql;
  }

  public void setUpSql(String upSql) {
    this.upSql = upSql;
  }

  public String getDownSql() {
    return downSql;
  }

  public void setDownSql(String downSql) {
    this.downSql = downSql;
  }

  public LocalDateTime getAppliedAt() {
    return appliedAt;
  }

  public void setAppliedAt(LocalDateTime appliedAt) {
    this.appliedAt = appliedAt;
  }
}
