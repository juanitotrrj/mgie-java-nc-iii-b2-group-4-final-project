package com.group4.inventoryserver;

import com.group4.inventoryserver.config.AppConfig;
import com.group4.inventoryserver.config.DatabaseConfig;
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
import com.group4.inventoryserver.migration.MigrationRunner;
import com.group4.inventoryserver.server.HttpServerBootstrap;
import com.group4.inventoryserver.server.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerLauncher {

  private static final Logger log = LoggerFactory.getLogger(ServerLauncher.class);

  private HttpServerBootstrap server;

  public void start(boolean runMigrations) {
    try {
      AppConfig.initialize();
      DatabaseConfig.initialize();

      if (runMigrations && EnvConfig.dbRunMigrationsOnStartup()) {
        log.info("Running database migrations...");
        new MigrationRunner().migrate();
      }

      Router router = buildRouter();
      server = new HttpServerBootstrap(router);
      server.start();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to start test server: " + e.getMessage(), e);
    }
  }

  public void stop() {
    if (server != null) {
      server.stop();
      server = null;
    }
    DatabaseConfig.shutdown();
  }

  public String getBaseUrl() {
    return "http://localhost:" + EnvConfig.serverPort() + EnvConfig.appContextPath();
  }

  public static Router buildRouter() {
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
    return router;
  }
}
