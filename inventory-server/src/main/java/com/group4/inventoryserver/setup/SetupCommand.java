package com.group4.inventoryserver.setup;

import com.group4.inventoryserver.config.DotEnvLoader;
import com.group4.inventoryserver.migration.MigrationRunner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class SetupCommand {

  private final Scanner scanner;
  private final Map<String, String> envValues = new LinkedHashMap<>();

  public SetupCommand() {
    this.scanner = new Scanner(System.in, "UTF-8");
  }

  public void run() {
    printBanner();

    File envFile = new File(".env");
    if (envFile.exists()) {
      System.out.println("[!] An existing .env file was detected.");
      System.out.print("    Overwrite and reconfigure? (y/N): ");
      String answer = scanner.nextLine().trim();
      if (!answer.equalsIgnoreCase("y")) {
        System.out.println("Setup cancelled. Use 'setup:resume' to continue a partial setup.");
        return;
      }
    }

    System.out.println();
    System.out.println("=== Step 1/5: Server Runtime ===");
    promptServerRuntime();

    System.out.println();
    System.out.println("=== Step 2/5: Database ===");
    promptDatabase();

    System.out.println();
    System.out.println("=== Step 3/5: File Storage Directories ===");
    promptDirectories();

    System.out.println();
    System.out.println("=== Step 4/5: Root User Credentials ===");
    String rootUsername = promptRootUser();

    System.out.println();
    System.out.println("=== Step 5/5: Confirmation ===");
    printSummary();
    System.out.print("Proceed with setup? (Y/n): ");
    String confirm = scanner.nextLine().trim();
    if (confirm.equalsIgnoreCase("n")) {
      System.out.println("Setup aborted.");
      return;
    }

    System.out.println();
    System.out.println("--- Writing .env file...");
    writeEnvFile(envFile);

    DotEnvLoader.reset();
    DotEnvLoader.load();

    System.out.println("--- Validating database connection...");
    if (!validateDbConnection()) {
      System.err.println("[ERROR] Could not connect to database. Please verify credentials.");
      System.err.println(
          "        The .env file has been written. Fix the values and run setup again.");
      return;
    }

    System.out.println("--- Creating schema if needed...");
    createSchemaIfNeeded();

    System.out.println("--- Running database migrations...");
    runMigrations();

    System.out.println("--- Creating root credentials...");
    createRootCredentials(rootUsername);

    System.out.println("--- Updating system state to INFRA_READY_APP_SETUP_PENDING...");
    updateSystemState();

    System.out.println();
    System.out.println("============================================================");
    System.out.println(" Setup Complete!");
    System.out.println("============================================================");
    System.out.println();
    System.out.println(" Next steps:");
    System.out.println("   1. Start the server:  java -jar inventory-server-1.0.0.jar serve");
    System.out.println("   2. Launch the client:  java -jar inventory-client-1.0.0.jar");
    System.out.println("   3. Complete the setup wizard in the client application.");
    System.out.println();
  }

  private void printBanner() {
    System.out.println("============================================================");
    System.out.println(" G4IMS Server - Initial Setup");
    System.out.println("============================================================");
    System.out.println(" This wizard will configure the server and prepare the");
    System.out.println(" database for first use.");
    System.out.println("============================================================");
    System.out.println();
  }

  private void promptServerRuntime() {
    envValues.put(
        "G4IMS_SERVER_APP_NAME",
        prompt("Application name", "Inventory Management System - Group 4 API"));
    envValues.put(
        "G4IMS_SERVER_APP_ENV", prompt("Environment (development/production)", "development"));
    envValues.put("G4IMS_SERVER_APP_VERSION", prompt("Server version", "1.0.0"));
    envValues.put("G4IMS_SERVER_APP_TIMEZONE", prompt("Timezone", "UTC"));
    envValues.put("G4IMS_SERVER_SERVER_HOST", prompt("Server bind host", "0.0.0.0"));
    envValues.put("G4IMS_SERVER_SERVER_PORT", prompt("Server port", "8080"));
    String host = envValues.get("G4IMS_SERVER_SERVER_HOST");
    String port = envValues.get("G4IMS_SERVER_SERVER_PORT");
    String baseUrl = "http://" + (host.equals("0.0.0.0") ? "localhost" : host) + ":" + port;
    envValues.put("G4IMS_SERVER_APP_BASE_URL", prompt("Base URL", baseUrl));
    envValues.put("G4IMS_SERVER_APP_CONTEXT_PATH", prompt("API context path", "/api"));
    String secret =
        UUID.randomUUID().toString().replace("-", "")
            + UUID.randomUUID().toString().replace("-", "");
    envValues.put(
        "G4IMS_SERVER_AUTH_TOKEN_SECRET", prompt("JWT token secret (auto-generated)", secret));
  }

  private void promptDatabase() {
    envValues.put("G4IMS_SERVER_DB_HOST", prompt("Database host", "localhost"));
    envValues.put("G4IMS_SERVER_DB_PORT", prompt("Database port", "3306"));
    envValues.put("G4IMS_SERVER_DB_NAME", prompt("Database/schema name", "g4ims_local"));
    envValues.put("G4IMS_SERVER_DB_USER", prompt("Database username", "root"));
    envValues.put("G4IMS_SERVER_DB_PASSWORD", promptPassword("Database password"));
    envValues.put("G4IMS_SERVER_DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    envValues.put(
        "G4IMS_SERVER_DB_RUN_MIGRATIONS_ON_STARTUP", prompt("Run migrations on startup?", "true"));

    String dbHost = envValues.get("G4IMS_SERVER_DB_HOST");
    String dbPort = envValues.get("G4IMS_SERVER_DB_PORT");
    String dbName = envValues.get("G4IMS_SERVER_DB_NAME");
    String url =
        "jdbc:mysql://"
            + dbHost
            + ":"
            + dbPort
            + "/"
            + dbName
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8";
    envValues.put("G4IMS_SERVER_DB_URL", url);
  }

  private void promptDirectories() {
    envValues.put("G4IMS_SERVER_FILE_STORAGE_ROOT", prompt("Storage root", "./storage"));
    envValues.put("G4IMS_SERVER_UPLOAD_DIR", prompt("Upload directory", "./storage/uploads"));
    envValues.put("G4IMS_SERVER_EXPORT_DIR", prompt("Export directory", "./storage/exports"));
    envValues.put("G4IMS_SERVER_BACKUP_DIR", prompt("Backup directory", "./storage/backups"));
    envValues.put("G4IMS_SERVER_LOG_DIR", prompt("Log directory", "./logs"));
  }

  private String promptRootUser() {
    String username = prompt("Root username", "root");
    String password;
    while (true) {
      password =
          promptPassword("Root password (min 8 chars, uppercase, lowercase, number, symbol)");
      if (password.length() >= 8) break;
      System.out.println("  [!] Password must be at least 8 characters.");
    }
    envValues.put("_ROOT_USERNAME", username);
    envValues.put("_ROOT_PASSWORD_HASH", BCrypt.hashpw(password, BCrypt.gensalt(10)));
    return username;
  }

  private void printSummary() {
    System.out.println(
        "  Server:   "
            + envValues.get("G4IMS_SERVER_APP_BASE_URL")
            + envValues.get("G4IMS_SERVER_APP_CONTEXT_PATH"));
    System.out.println(
        "  Database: "
            + envValues.get("G4IMS_SERVER_DB_HOST")
            + ":"
            + envValues.get("G4IMS_SERVER_DB_PORT")
            + "/"
            + envValues.get("G4IMS_SERVER_DB_NAME"));
    System.out.println("  Storage:  " + envValues.get("G4IMS_SERVER_FILE_STORAGE_ROOT"));
    System.out.println("  Root user: " + envValues.get("_ROOT_USERNAME"));
    System.out.println();
  }

  private void writeEnvFile(File envFile) {
    try (BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(envFile), "UTF-8"))) {
      writer.write("# G4IMS Server Configuration (generated by setup)");
      writer.newLine();
      writer.write("# Generated at: " + new java.sql.Timestamp(System.currentTimeMillis()));
      writer.newLine();
      writer.newLine();
      for (Map.Entry<String, String> entry : envValues.entrySet()) {
        if (entry.getKey().startsWith("_")) continue;
        writer.write(entry.getKey() + "=" + entry.getValue());
        writer.newLine();
      }
      writer.newLine();
      writer.write("# Defaults not prompted (edit as needed):");
      writer.newLine();
      writeDefault(writer, "G4IMS_SERVER_SERVER_BACKLOG", "100");
      writeDefault(writer, "G4IMS_SERVER_SERVER_MAX_REQUEST_BODY_BYTES", "10485760");
      writeDefault(writer, "G4IMS_SERVER_SERVER_REQUEST_TIMEOUT_SECONDS", "30");
      writeDefault(writer, "G4IMS_SERVER_DB_POOL_MIN_SIZE", "2");
      writeDefault(writer, "G4IMS_SERVER_DB_POOL_MAX_SIZE", "10");
      writeDefault(writer, "G4IMS_SERVER_DB_POOL_CONNECTION_TIMEOUT_MS", "30000");
      writeDefault(writer, "G4IMS_SERVER_DB_POOL_IDLE_TIMEOUT_MS", "600000");
      writeDefault(writer, "G4IMS_SERVER_DB_POOL_MAX_LIFETIME_MS", "1800000");
      writeDefault(writer, "G4IMS_SERVER_AUTH_TOKEN_EXPIRY_SECONDS", "1800");
      writeDefault(writer, "G4IMS_SERVER_AUTH_REFRESH_TOKEN_EXPIRY_SECONDS", "86400");
      writeDefault(writer, "G4IMS_SERVER_PASSWORD_BCRYPT_COST", "10");
      writeDefault(writer, "G4IMS_SERVER_PASSWORD_MIN_LENGTH", "8");
      writeDefault(writer, "G4IMS_SERVER_CORS_ENABLED", "true");
      writeDefault(
          writer,
          "G4IMS_SERVER_CORS_ALLOWED_ORIGINS",
          "http://localhost:3000,http://localhost:8080");
      writeDefault(
          writer, "G4IMS_SERVER_CORS_ALLOWED_METHODS", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
      writeDefault(
          writer,
          "G4IMS_SERVER_CORS_ALLOWED_HEADERS",
          "Authorization,Content-Type,Accept,X-Setup-Token");
      writeDefault(writer, "G4IMS_SERVER_LOG_LEVEL", "INFO");
      writeDefault(writer, "G4IMS_SERVER_SWAGGER_ENABLED", "true");
      writeDefault(writer, "G4IMS_SERVER_AUDIT_LOG_ENABLED", "true");
      System.out.println("    .env written successfully.");
    } catch (IOException e) {
      System.err.println("[ERROR] Failed to write .env: " + e.getMessage());
    }
  }

  private void writeDefault(BufferedWriter writer, String key, String value) throws IOException {
    if (!envValues.containsKey(key)) {
      writer.write(key + "=" + value);
      writer.newLine();
    }
  }

  private boolean validateDbConnection() {
    String host = envValues.get("G4IMS_SERVER_DB_HOST");
    String port = envValues.get("G4IMS_SERVER_DB_PORT");
    String user = envValues.get("G4IMS_SERVER_DB_USER");
    String password = envValues.get("G4IMS_SERVER_DB_PASSWORD");
    String url =
        "jdbc:mysql://"
            + host
            + ":"
            + port
            + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      try (Connection conn = DriverManager.getConnection(url, user, password)) {
        return conn.isValid(5);
      }
    } catch (ClassNotFoundException | SQLException e) {
      System.err.println("    Connection error: " + e.getMessage());
      return false;
    }
  }

  private void createSchemaIfNeeded() {
    String host = envValues.get("G4IMS_SERVER_DB_HOST");
    String port = envValues.get("G4IMS_SERVER_DB_PORT");
    String user = envValues.get("G4IMS_SERVER_DB_USER");
    String password = envValues.get("G4IMS_SERVER_DB_PASSWORD");
    String dbName = envValues.get("G4IMS_SERVER_DB_NAME");
    String url =
        "jdbc:mysql://"
            + host
            + ":"
            + port
            + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    try (Connection conn = DriverManager.getConnection(url, user, password);
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(
          "CREATE DATABASE IF NOT EXISTS `"
              + dbName
              + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
      System.out.println("    Schema '" + dbName + "' ready.");
    } catch (SQLException e) {
      System.err.println("[ERROR] Could not create schema: " + e.getMessage());
    }
  }

  private void runMigrations() {
    try {
      com.group4.inventoryserver.config.DatabaseConfig.initialize();
      new MigrationRunner().migrate();
    } catch (Exception e) {
      System.err.println("[ERROR] Migration failed: " + e.getMessage());
    }
  }

  private void createRootCredentials(String rootUsername) {
    String hash = envValues.get("_ROOT_PASSWORD_HASH");
    try (Connection conn = com.group4.inventoryserver.config.DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "INSERT INTO system_root_credentials (username, password_hash) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), "
                    + "updated_at = CURRENT_TIMESTAMP")) {
      ps.setString(1, rootUsername);
      ps.setString(2, hash);
      ps.executeUpdate();
      System.out.println("    Root credentials stored.");
    } catch (SQLException e) {
      System.err.println("[ERROR] Could not store root credentials: " + e.getMessage());
    }
  }

  private void updateSystemState() {
    try (Connection conn = com.group4.inventoryserver.config.DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "UPDATE system_installation SET setup_state = 'INFRA_READY_APP_SETUP_PENDING', "
                    + "installed_at = CURRENT_TIMESTAMP WHERE installation_id = 1")) {
      ps.executeUpdate();
      System.out.println("    System state updated.");
    } catch (SQLException e) {
      System.err.println("[ERROR] Could not update system state: " + e.getMessage());
    }
  }

  private String prompt(String label, String defaultValue) {
    if (defaultValue != null && !defaultValue.isEmpty()) {
      System.out.print("  " + label + " [" + defaultValue + "]: ");
    } else {
      System.out.print("  " + label + ": ");
    }
    String input = scanner.nextLine().trim();
    return (input.isEmpty() && defaultValue != null) ? defaultValue : input;
  }

  private String promptPassword(String label) {
    java.io.Console console = System.console();
    if (console != null) {
      char[] chars = console.readPassword("  %s: ", label);
      return new String(chars);
    }
    System.out.print("  " + label + ": ");
    return scanner.nextLine().trim();
  }
}
