package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import com.group4.inventoryserver.util.DatabaseBackupUtil.BackupInfo;
import org.junit.Test;

public class DatabaseBackupUtilTest {

  @Test
  public void backupInfo_getters() {
    BackupInfo info = new BackupInfo("db_backup.sql", "/tmp/db_backup.sql", 4096L);

    assertEquals("db_backup.sql", info.getFilename());
    assertEquals("/tmp/db_backup.sql", info.getPath());
    assertEquals(4096L, info.getSizeBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void restore_throwsWhenFileMissing() {
    DatabaseBackupUtil.restore("/nonexistent/path/backup.sql");
  }
}
