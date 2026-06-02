package com.group4.inventoryserver;

import com.group4.inventoryserver.config.AppConfig;
import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.handler.AboutHandler;
import com.group4.inventoryserver.handler.CategoryHandler;
import com.group4.inventoryserver.handler.ContactHandler;
import com.group4.inventoryserver.handler.CurrentUserHandler;
import com.group4.inventoryserver.handler.HealthHandler;
import com.group4.inventoryserver.handler.InquiryHandler;
import com.group4.inventoryserver.handler.LoginHandler;
import com.group4.inventoryserver.handler.LogoutHandler;
import com.group4.inventoryserver.handler.ProductHandler;
import com.group4.inventoryserver.handler.SupplierHandler;
import com.group4.inventoryserver.handler.WelcomeHandler;
import com.group4.inventoryserver.migration.MigrationGenerator;
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.server.HttpServerBootstrap;
import com.group4.inventoryserver.server.Router;
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
        System.err.println("Available commands:");
        System.err.println("  (no args)          Start the server");
        System.err.println("  migrate            Run pending migrations");
        System.err.println("  migrate:status     Show migration status");
        System.err.println("  migrate:rollback   Rollback last migration");
        System.err.println("  migrate:generate   Generate a new migration file");
        System.exit(1);
    }
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
      router.register(contextPath + "/health", new HealthHandler());
      router.register(contextPath + "/public/welcome", new WelcomeHandler());
      router.register(contextPath + "/public/about", new AboutHandler());
      router.register(contextPath + "/public/contact", new ContactHandler());
      router.register(contextPath + "/public/inquiries", new InquiryHandler());
      router.register(contextPath + "/auth/login", new LoginHandler());
      router.register(contextPath + "/auth/logout", new LogoutHandler());
      router.register(contextPath + "/auth/me", new CurrentUserHandler());
      router.register(contextPath + "/products", new ProductHandler());
      router.register(contextPath + "/categories", new CategoryHandler());
      router.register(contextPath + "/suppliers", new SupplierHandler());

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
