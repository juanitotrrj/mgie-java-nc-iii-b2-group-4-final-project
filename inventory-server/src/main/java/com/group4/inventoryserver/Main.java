package com.group4.inventoryserver;

import com.group4.inventoryserver.config.AppConfig;
import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.DotEnvLoader;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.handler.AboutHandler;
import com.group4.inventoryserver.handler.AuditLogHandler;
import com.group4.inventoryserver.handler.CategoryHandler;
import com.group4.inventoryserver.handler.ContactHandler;
import com.group4.inventoryserver.handler.CurrentUserHandler;
import com.group4.inventoryserver.handler.DashboardHandler;
import com.group4.inventoryserver.handler.HealthHandler;
import com.group4.inventoryserver.handler.IcrHandler;
import com.group4.inventoryserver.handler.InquiryHandler;
import com.group4.inventoryserver.handler.LoginHandler;
import com.group4.inventoryserver.handler.LogoutHandler;
import com.group4.inventoryserver.handler.ProductHandler;
import com.group4.inventoryserver.handler.PurchaseHandler;
import com.group4.inventoryserver.handler.ReportHandler;
import com.group4.inventoryserver.handler.SaleHandler;
import com.group4.inventoryserver.handler.SettingsHandler;
import com.group4.inventoryserver.handler.SetupHandler;
import com.group4.inventoryserver.handler.StockMovementHandler;
import com.group4.inventoryserver.handler.SupplierHandler;
import com.group4.inventoryserver.handler.SwaggerHandler;
import com.group4.inventoryserver.handler.UserHandler;
import com.group4.inventoryserver.handler.WelcomeHandler;
import com.group4.inventoryserver.migration.MigrationGenerator;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.server.HttpServerBootstrap;
import com.group4.inventoryserver.server.Router;
import com.group4.inventoryserver.setup.SetupCommand;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    if (args.length > 0) {
      handleCommand(args);
      return;
    }

    startServer();
  }

  private static void handleCommand(String[] args) {
    String command = args[0];
    switch (command) {
      case "serve":
        startServer();
        break;

      case "setup":
        new SetupCommand().run();
        break;

      case "status":
        handleStatus();
        break;

      case "health":
        handleHealth();
        break;

      case "config:validate":
        handleConfigValidate();
        break;

      case "setup:resume":
        new SetupCommand().run();
        break;

      case "setup:unlock":
        handleSetupUnlock();
        break;

      case "setup:reset":
        handleSetupReset(args);
        break;

      case "migrate":
        DatabaseConfig.initialize();
        new MigrationRunner().migrate();
        DatabaseConfig.shutdown();
        break;

      case "migrate:status":
        DatabaseConfig.initialize();
        new MigrationRunner().status();
        DatabaseConfig.shutdown();
        break;

      case "migrate:rollback":
        DatabaseConfig.initialize();
        new MigrationRunner().rollback();
        DatabaseConfig.shutdown();
        break;

      case "migrate:generate":
        if (args.length < 2) {
          System.err.println("Usage: migrate:generate <description>");
          System.err.println("Example: migrate:generate create_users_table");
          System.exit(1);
        }
        new MigrationGenerator().generate(args[1]);
        break;

      default:
        System.err.println("Unknown command: " + command);
        printUsage();
        System.exit(1);
    }
  }

  private static void printUsage() {
    System.err.println("Available commands:");
    System.err.println("  serve              Start the server");
    System.err.println("  setup              Run initial setup wizard");
    System.err.println("  status             Show server and database status");
    System.err.println("  health             Check health (exit code 0=ok, 1=error)");
    System.err.println("  config:validate    Validate .env configuration");
    System.err.println("  setup:resume       Resume a partial setup");
    System.err.println("  setup:unlock       Unlock setup (requires root auth)");
    System.err.println("  setup:reset        Reset setup (dev only)");
    System.err.println("  migrate            Run pending migrations");
    System.err.println("  migrate:status     Show migration status");
    System.err.println("  migrate:rollback   Rollback last migration");
    System.err.println("  migrate:generate   Generate a new migration file");
  }

  private static void handleStatus() {
    File envFile = new File(".env");
    System.out.println("=== G4IMS Server Status ===");
    System.out.println("  .env file:  " + (envFile.exists() ? "found" : "NOT FOUND"));
    System.out.println("  App env:    " + EnvConfig.appEnv());
    System.out.println("  Base URL:   " + EnvConfig.appBaseUrl());
    System.out.println("  DB host:    " + EnvConfig.dbHost() + ":" + EnvConfig.dbPort());
    System.out.println("  DB name:    " + EnvConfig.dbName());
    System.out.println("  DB user:    " + EnvConfig.dbUser());
    System.out.println("  DB pass:    " + maskSecret(EnvConfig.dbPassword()));

    try {
      DatabaseConfig.initialize();
      try (Connection conn = DatabaseConfig.getConnection()) {
        System.out.println("  DB status:  CONNECTED");
      }
      String state = querySetupState();
      System.out.println("  Setup state: " + (state != null ? state : "UNKNOWN (table missing?)"));
      DatabaseConfig.shutdown();
    } catch (Exception e) {
      System.out.println("  DB status:  DISCONNECTED (" + e.getMessage() + ")");
    }
  }

  private static void handleHealth() {
    try {
      DatabaseConfig.initialize();
      try (Connection conn = DatabaseConfig.getConnection()) {
        if (!conn.isValid(5)) {
          System.err.println("UNHEALTHY: DB connection invalid");
          System.exit(1);
        }
      }
      DatabaseConfig.shutdown();
      System.out.println("OK");
    } catch (Exception e) {
      System.err.println("UNHEALTHY: " + e.getMessage());
      System.exit(1);
    }
  }

  private static void handleConfigValidate() {
    DotEnvLoader.load();
    String[] required = {
      "G4IMS_SERVER_DB_HOST", "G4IMS_SERVER_DB_PORT", "G4IMS_SERVER_DB_NAME",
      "G4IMS_SERVER_DB_USER", "G4IMS_SERVER_DB_PASSWORD", "G4IMS_SERVER_AUTH_TOKEN_SECRET"
    };
    boolean valid = true;
    for (String key : required) {
      String val = DotEnvLoader.resolve(key, null);
      if (val == null || val.isEmpty()) {
        System.err.println("  MISSING: " + key);
        valid = false;
      }
    }
    if (valid) {
      System.out.println("Configuration is valid. All required keys present.");
    } else {
      System.err.println("Configuration has errors. Fix the .env file.");
      System.exit(1);
    }
  }

  @SuppressWarnings("resource")
  private static void handleSetupUnlock() {
    java.util.Scanner sc = new java.util.Scanner(System.in, "UTF-8");
    System.out.print("Root username: ");
    String username = sc.nextLine().trim();
    System.out.print("Root password: ");
    String password = sc.nextLine().trim();

    try {
      DatabaseConfig.initialize();
      try (Connection conn = DatabaseConfig.getConnection();
          PreparedStatement ps =
              conn.prepareStatement(
                  "SELECT password_hash FROM system_root_credentials WHERE username = ?")) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            System.err.println("Invalid credentials.");
            System.exit(1);
          }
          String hash = rs.getString("password_hash");
          if (!org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) {
            System.err.println("Invalid credentials.");
            System.exit(1);
          }
        }
      }
      try (Connection conn = DatabaseConfig.getConnection();
          PreparedStatement ps =
              conn.prepareStatement(
                  "UPDATE system_installation SET setup_state = 'INFRA_READY_APP_SETUP_PENDING' "
                      + "WHERE installation_id = 1")) {
        ps.executeUpdate();
      }
      DatabaseConfig.shutdown();
      System.out.println("Setup unlocked. State reset to INFRA_READY_APP_SETUP_PENDING.");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static void handleSetupReset(String[] args) {
    boolean devOnly = false;
    for (String arg : args) {
      if ("--dev-only".equals(arg)) devOnly = true;
    }
    if (!devOnly || !"development".equalsIgnoreCase(EnvConfig.appEnv())) {
      System.err.println("setup:reset requires --dev-only flag and APP_ENV=development");
      System.exit(1);
    }
    try {
      DatabaseConfig.initialize();
      try (Connection conn = DatabaseConfig.getConnection();
          java.sql.Statement stmt = conn.createStatement()) {
        stmt.executeUpdate("DELETE FROM setup_sessions");
        stmt.executeUpdate("DELETE FROM system_root_credentials");
        stmt.executeUpdate(
            "UPDATE system_installation SET setup_state = 'UNCONFIGURED', "
                + "installed_at = NULL, initialized_at = NULL WHERE installation_id = 1");
      }
      DatabaseConfig.shutdown();
      System.out.println("Setup state reset to UNCONFIGURED (dev mode).");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static String querySetupState() {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT setup_state FROM system_installation WHERE installation_id = 1");
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getString("setup_state");
    } catch (Exception e) {
      // table may not exist yet
    }
    return null;
  }

  private static String maskSecret(String value) {
    if (value == null || value.length() <= 4) return "****";
    return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
  }

  private static void startServer() {
    try {
      AppConfig.initialize();
      DatabaseConfig.initialize();

      if (EnvConfig.dbRunMigrationsOnStartup()) {
        log.info("Running database migrations on startup...");
        new MigrationRunner().migrate();
      }

      Router router = new Router();
      String contextPath = EnvConfig.appContextPath();
      router.register(contextPath + "/setup", new SetupHandler());
      router.register(contextPath + "/health", new HealthHandler());
      router.register(contextPath + "/public/welcome", new WelcomeHandler());
      router.register(contextPath + "/public/about", new AboutHandler());
      router.register(contextPath + "/public/contact", new ContactHandler());
      router.register(contextPath + "/public/inquiries", new InquiryHandler());
      if (EnvConfig.swaggerEnabled()) {
        router.register(contextPath + "/docs", new SwaggerHandler());
      }
      router.register(contextPath + "/auth/login", new LoginHandler());
      router.register(contextPath + "/auth/logout", new LogoutHandler());
      router.register(contextPath + "/auth/me", new CurrentUserHandler());
      router.register(contextPath + "/dashboard", new DashboardHandler());
      router.register(contextPath + "/products", new ProductHandler());
      router.register(contextPath + "/categories", new CategoryHandler());
      router.register(contextPath + "/suppliers", new SupplierHandler());
      router.register(contextPath + "/purchases", new PurchaseHandler());
      router.register(contextPath + "/sales", new SaleHandler());
      router.register(contextPath + "/users", new UserHandler());
      router.register(contextPath + "/inventory-change-requests", new IcrHandler());
      router.register(contextPath + "/stock-movements", new StockMovementHandler());
      router.register(contextPath + "/reports", new ReportHandler());
      router.register(contextPath + "/settings", new SettingsHandler());
      router.register(contextPath + "/audit-logs", new AuditLogHandler());

      HttpServerBootstrap server = new HttpServerBootstrap(router);
      server.start();

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    log.info("Shutting down...");
                    server.stop();
                    DatabaseConfig.shutdown();
                  }));

    } catch (Exception e) {
      log.error("Failed to start server: {}", e.getMessage(), e);
      System.exit(1);
    }
  }
}
