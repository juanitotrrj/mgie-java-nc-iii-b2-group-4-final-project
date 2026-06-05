package com.group4.inventoryserver.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import org.junit.After;
import org.junit.Test;

public class AppConfigTest {

  @After
  public void tearDown() {
    DotEnvLoader.reset();
    System.clearProperty("g4ims.env.file");
  }

  @Test
  public void isDevelopment_trueForDevelopmentEnv() throws Exception {
    loadEnv("G4IMS_SERVER_APP_ENV=development\n");

    assertTrue(AppConfig.isDevelopment());
    assertFalse(AppConfig.isProduction());
    assertFalse(AppConfig.isTest());
  }

  @Test
  public void isProduction_trueForProductionEnv() throws Exception {
    loadEnv("G4IMS_SERVER_APP_ENV=production\n");

    assertTrue(AppConfig.isProduction());
    assertFalse(AppConfig.isDevelopment());
  }

  @Test
  public void isTest_trueForTestEnv() throws Exception {
    loadEnv("G4IMS_SERVER_APP_ENV=test\n");

    assertTrue(AppConfig.isTest());
  }

  @Test
  public void initialize_createsStorageDirectories() throws Exception {
    File root =
        new File(System.getProperty("java.io.tmpdir"), "g4ims-appconfig-" + System.nanoTime());
    root.mkdirs();
    String rootPath = root.getAbsolutePath();
    loadEnv(
        "G4IMS_SERVER_FILE_STORAGE_ROOT="
            + rootPath
            + "\n"
            + "G4IMS_SERVER_UPLOAD_DIR="
            + rootPath
            + "/uploads\n"
            + "G4IMS_SERVER_EXPORT_DIR="
            + rootPath
            + "/exports\n"
            + "G4IMS_SERVER_BACKUP_DIR="
            + rootPath
            + "/backups\n"
            + "G4IMS_SERVER_TEMP_DIR="
            + rootPath
            + "/tmp\n"
            + "G4IMS_SERVER_LOG_DIR="
            + rootPath
            + "/logs\n"
            + "G4IMS_SERVER_INVENTORY_PROOF_UPLOAD_DIR="
            + rootPath
            + "/proofs\n");

    AppConfig.initialize();

    assertTrue(new File(rootPath + "/uploads").exists());
    assertTrue(new File(rootPath + "/exports").exists());
    assertTrue(new File(rootPath + "/backups").exists());
    assertTrue(new File(rootPath + "/logs").exists());
  }

  private void loadEnv(String content) throws Exception {
    File temp = File.createTempFile("g4ims-app", ".env");
    temp.deleteOnExit();
    try (FileWriter w = new FileWriter(temp)) {
      w.write(content);
    }
    System.setProperty("g4ims.env.file", temp.getAbsolutePath());
    DotEnvLoader.reset();
    DotEnvLoader.load();
  }
}
