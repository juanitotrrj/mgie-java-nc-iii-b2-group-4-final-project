package com.group4.inventoryserver.testing;

import com.group4.inventoryserver.config.DotEnvLoader;
import java.io.File;

public final class TestEnv {

  private TestEnv() {}

  public static void loadTestEnv() {
    String path = System.getProperty("g4ims.env.file", ".env.test");
    File f = new File(path);
    if (!f.isAbsolute()) {
      f = new File("inventory-server", path);
      if (!f.exists()) {
        f = new File(path);
      }
    }
    System.setProperty("g4ims.env.file", f.getPath());
    DotEnvLoader.reset();
    DotEnvLoader.load();
  }

  public static String serverBaseUrl() {
    String override = System.getProperty("e2e.server.url");
    if (override != null && !override.isEmpty()) {
      return override.endsWith("/") ? override.substring(0, override.length() - 1) : override;
    }
    return "http://localhost:18080/api";
  }

  public static boolean isMysqlAvailable() {
    String host = System.getenv("G4IMS_TEST_DB_HOST");
    if (host == null) host = "127.0.0.1";
    String port = System.getenv("G4IMS_TEST_DB_PORT");
    if (port == null) port = "3307";
    try (java.net.Socket socket = new java.net.Socket()) {
      socket.connect(new java.net.InetSocketAddress(host, Integer.parseInt(port)), 2000);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
