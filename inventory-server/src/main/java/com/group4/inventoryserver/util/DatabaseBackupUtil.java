package com.group4.inventoryserver.util;

import com.group4.inventoryserver.config.EnvConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseBackupUtil {

  private static final Logger log = LoggerFactory.getLogger(DatabaseBackupUtil.class);

  private DatabaseBackupUtil() {}

  public static BackupInfo backup() {
    String backupDir = EnvConfig.backupDir();
    File dir = new File(backupDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String filename = EnvConfig.dbName() + "_backup_" + timestamp + ".sql";
    File backupFile = new File(dir, filename);

    String[] command = {
      EnvConfig.mysqldumpBin(),
      "--host=" + EnvConfig.dbHost(),
      "--port=" + EnvConfig.dbPort(),
      "--user=" + EnvConfig.dbUser(),
      "--password=" + EnvConfig.dbPassword(),
      "--single-transaction",
      "--routines",
      "--triggers",
      "--column-statistics=0",
      "--result-file=" + backupFile.getAbsolutePath(),
      EnvConfig.dbName()
    };

    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectErrorStream(true);
      Process process = pb.start();
      String output = readProcessOutput(process);
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.error("mysqldump failed (exit {}): {}", exitCode, output);
        throw new RuntimeException("Database backup failed: " + output);
      }
      long fileSize = backupFile.exists() ? backupFile.length() : 0;
      log.info("Database backup created: {} ({} bytes)", filename, fileSize);
      return new BackupInfo(filename, backupFile.getAbsolutePath(), fileSize);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Database backup failed: " + e.getMessage(), e);
    }
  }

  public static void restore(String backupFilePath) {
    File backupFile = new File(backupFilePath);
    if (!backupFile.exists()) {
      throw new IllegalArgumentException("Backup file not found: " + backupFilePath);
    }

    String[] command = {
      EnvConfig.mysqlBin(),
      "--host=" + EnvConfig.dbHost(),
      "--port=" + EnvConfig.dbPort(),
      "--user=" + EnvConfig.dbUser(),
      "--password=" + EnvConfig.dbPassword(),
      EnvConfig.dbName()
    };

    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectInput(backupFile);
      pb.redirectErrorStream(true);
      Process process = pb.start();
      String output = readProcessOutput(process);
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.error("mysql restore failed (exit {}): {}", exitCode, output);
        throw new RuntimeException("Database restore failed: " + output);
      }
      log.info("Database restored from: {}", backupFilePath);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Database restore failed: " + e.getMessage(), e);
    }
  }

  private static String readProcessOutput(Process process) throws Exception {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString().trim();
  }

  public static class BackupInfo {
    private final String filename;
    private final String path;
    private final long sizeBytes;

    public BackupInfo(String filename, String path, long sizeBytes) {
      this.filename = filename;
      this.path = path;
      this.sizeBytes = sizeBytes;
    }

    public String getFilename() {
      return filename;
    }

    public String getPath() {
      return path;
    }

    public long getSizeBytes() {
      return sizeBytes;
    }
  }
}
