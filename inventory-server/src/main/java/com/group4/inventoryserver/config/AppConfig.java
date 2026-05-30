package com.group4.inventoryserver.config;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppConfig {

  private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

  private AppConfig() {}

  public static void initialize() {
    log.info(
        "Initializing application: {} v{} [{}]",
        EnvConfig.appName(),
        EnvConfig.appVersion(),
        EnvConfig.appEnv());

    ensureDirectoryExists(EnvConfig.fileStorageRoot());
    ensureDirectoryExists(EnvConfig.uploadDir());
    ensureDirectoryExists(EnvConfig.inventoryProofUploadDir());
    ensureDirectoryExists(EnvConfig.exportDir());
    ensureDirectoryExists(EnvConfig.backupDir());
    ensureDirectoryExists(EnvConfig.tempDir());
    ensureDirectoryExists(EnvConfig.logDir());

    if (isProduction() && EnvConfig.authTokenSecret().startsWith("replace_with")) {
      log.warn("AUTH_TOKEN_SECRET is still using the default value in production!");
    }
  }

  public static boolean isProduction() {
    return "production".equalsIgnoreCase(EnvConfig.appEnv());
  }

  public static boolean isDevelopment() {
    return "development".equalsIgnoreCase(EnvConfig.appEnv());
  }

  public static boolean isTest() {
    return "test".equalsIgnoreCase(EnvConfig.appEnv());
  }

  private static void ensureDirectoryExists(String path) {
    File dir = new File(path);
    if (!dir.exists() && dir.mkdirs()) {
      log.debug("Created directory: {}", dir.getAbsolutePath());
    }
  }
}
