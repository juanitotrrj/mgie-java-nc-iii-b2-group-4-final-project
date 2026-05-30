package com.group4.inventoryserver.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class EnvConfig {

  private static final String PREFIX = "G4IMS_SERVER_";

  private EnvConfig() {}

  // ─── Application / Runtime ──────────────────────────────────────────────────

  public static String appName() {
    return get("APP_NAME", "Inventory Management System - Group 4 API");
  }

  public static String appEnv() {
    return get("APP_ENV", "development");
  }

  public static String appVersion() {
    return get("APP_VERSION", "1.0.0");
  }

  public static String appTimezone() {
    return get("APP_TIMEZONE", "UTC");
  }

  public static String appBaseUrl() {
    return get("APP_BASE_URL", "http://localhost:8080");
  }

  public static String appContextPath() {
    return get("APP_CONTEXT_PATH", "/api");
  }

  public static String serverHost() {
    return get("SERVER_HOST", "0.0.0.0");
  }

  public static int serverPort() {
    return getInt("SERVER_PORT", 8080);
  }

  public static int serverBacklog() {
    return getInt("SERVER_BACKLOG", 100);
  }

  public static int serverMaxRequestBodyBytes() {
    return getInt("SERVER_MAX_REQUEST_BODY_BYTES", 10485760);
  }

  public static int serverMaxUploadBytes() {
    return getInt("SERVER_MAX_UPLOAD_BYTES", 5242880);
  }

  public static int serverRequestTimeoutSeconds() {
    return getInt("SERVER_REQUEST_TIMEOUT_SECONDS", 30);
  }

  // ─── Database ───────────────────────────────────────────────────────────────

  public static String dbDriver() {
    return get("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
  }

  public static String dbHost() {
    return get("DB_HOST", "localhost");
  }

  public static int dbPort() {
    return getInt("DB_PORT", 3306);
  }

  public static String dbName() {
    return get("DB_NAME", "inventory_system_group4");
  }

  public static String dbUser() {
    return get("DB_USER", "inventory_user");
  }

  public static String dbPassword() {
    return get("DB_PASSWORD", "change_me_secure_password");
  }

  public static String dbUrl() {
    String explicit = get("DB_URL", null);
    if (explicit != null) {
      return explicit;
    }
    return String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8",
        dbHost(), dbPort(), dbName());
  }

  public static int dbPoolMinSize() {
    return getInt("DB_POOL_MIN_SIZE", 2);
  }

  public static int dbPoolMaxSize() {
    return getInt("DB_POOL_MAX_SIZE", 10);
  }

  public static long dbPoolConnectionTimeoutMs() {
    return getLong("DB_POOL_CONNECTION_TIMEOUT_MS", 30000L);
  }

  public static long dbPoolIdleTimeoutMs() {
    return getLong("DB_POOL_IDLE_TIMEOUT_MS", 600000L);
  }

  public static long dbPoolMaxLifetimeMs() {
    return getLong("DB_POOL_MAX_LIFETIME_MS", 1800000L);
  }

  public static boolean dbRunMigrationsOnStartup() {
    return getBool("DB_RUN_MIGRATIONS_ON_STARTUP", false);
  }

  public static boolean dbRunSeedOnStartup() {
    return getBool("DB_RUN_SEED_ON_STARTUP", false);
  }

  // ─── Authentication / Security ──────────────────────────────────────────────

  public static String authTokenType() {
    return get("AUTH_TOKEN_TYPE", "Bearer");
  }

  public static String authTokenSecret() {
    return get("AUTH_TOKEN_SECRET", "replace_with_long_random_secret_at_least_32_chars");
  }

  public static int authTokenExpirySeconds() {
    return getInt("AUTH_TOKEN_EXPIRY_SECONDS", 1800);
  }

  public static int authRefreshTokenExpirySeconds() {
    return getInt("AUTH_REFRESH_TOKEN_EXPIRY_SECONDS", 86400);
  }

  public static String passwordHashAlgorithm() {
    return get("PASSWORD_HASH_ALGORITHM", "BCrypt");
  }

  public static int passwordBcryptCost() {
    return getInt("PASSWORD_BCRYPT_COST", 10);
  }

  public static int passwordMinLength() {
    return getInt("PASSWORD_MIN_LENGTH", 8);
  }

  public static boolean passwordRequireUppercase() {
    return getBool("PASSWORD_REQUIRE_UPPERCASE", true);
  }

  public static boolean passwordRequireLowercase() {
    return getBool("PASSWORD_REQUIRE_LOWERCASE", true);
  }

  public static boolean passwordRequireNumber() {
    return getBool("PASSWORD_REQUIRE_NUMBER", true);
  }

  public static boolean passwordRequireSymbol() {
    return getBool("PASSWORD_REQUIRE_SYMBOL", true);
  }

  public static int securitySessionTimeoutMinutes() {
    return getInt("SECURITY_SESSION_TIMEOUT_MINUTES", 30);
  }

  public static boolean securityAllowMultipleSessions() {
    return getBool("SECURITY_ALLOW_MULTIPLE_SESSIONS", false);
  }

  public static int securityLockAfterFailedAttempts() {
    return getInt("SECURITY_LOCK_AFTER_FAILED_ATTEMPTS", 5);
  }

  public static int securityPasswordExpiryDays() {
    return getInt("SECURITY_PASSWORD_EXPIRY_DAYS", 90);
  }

  public static boolean securityRequireLoginOnStartup() {
    return getBool("SECURITY_REQUIRE_LOGIN_ON_STARTUP", true);
  }

  // ─── Default Admin Bootstrap ────────────────────────────────────────────────

  public static String defaultAdminUsername() {
    return get("DEFAULT_ADMIN_USERNAME", "admin");
  }

  public static String defaultAdminPassword() {
    return get("DEFAULT_ADMIN_PASSWORD", "Admin@123");
  }

  public static String defaultAdminEmail() {
    return get("DEFAULT_ADMIN_EMAIL", "admin@inventory.local");
  }

  public static String defaultAdminFullName() {
    return get("DEFAULT_ADMIN_FULL_NAME", "System Administrator");
  }

  // ─── Business Rules ─────────────────────────────────────────────────────────

  public static int inventoryLowStockThreshold() {
    return getInt("INVENTORY_LOW_STOCK_THRESHOLD", 10);
  }

  public static double inventoryReorderMultiplier() {
    return getDouble("INVENTORY_REORDER_MULTIPLIER", 1.50);
  }

  public static String inventoryDefaultProductStatus() {
    return get("INVENTORY_DEFAULT_PRODUCT_STATUS", "In Stock");
  }

  public static String inventoryCostingMethod() {
    return get("INVENTORY_COSTING_METHOD", "Average Cost");
  }

  public static boolean inventoryAllowNegativeStock() {
    return getBool("INVENTORY_ALLOW_NEGATIVE_STOCK", false);
  }

  public static boolean inventoryAutoUpdateTotalValues() {
    return getBool("INVENTORY_AUTO_UPDATE_TOTAL_VALUES", true);
  }

  public static boolean inventoryWarnBelowThreshold() {
    return getBool("INVENTORY_WARN_BELOW_THRESHOLD", true);
  }

  public static boolean inventoryTrackExpirationDates() {
    return getBool("INVENTORY_TRACK_EXPIRATION_DATES", false);
  }

  public static int inventoryDefaultExpiryWarningDays() {
    return getInt("INVENTORY_DEFAULT_EXPIRY_WARNING_DAYS", 30);
  }

  public static boolean inventoryClerkChangesRequireManagerApproval() {
    return getBool("INVENTORY_CLERK_CHANGES_REQUIRE_MANAGER_APPROVAL", true);
  }

  public static boolean salesAllowPendingInvoice() {
    return getBool("SALES_ALLOW_PENDING_INVOICE", true);
  }

  public static boolean salesAllowCancelPaidInvoice() {
    return getBool("SALES_ALLOW_CANCEL_PAID_INVOICE", true);
  }

  public static boolean purchaseAllowReceivePendingOnly() {
    return getBool("PURCHASE_ALLOW_RECEIVE_PENDING_ONLY", true);
  }

  public static boolean purchaseAllowCancelReceived() {
    return getBool("PURCHASE_ALLOW_CANCEL_RECEIVED", false);
  }

  public static String currencyName() {
    return get("CURRENCY_NAME", "Philippine Peso");
  }

  public static String currencyCode() {
    return get("CURRENCY_CODE", "PHP");
  }

  public static String currencySymbol() {
    return get("CURRENCY_SYMBOL", "\u20B1");
  }

  public static double defaultTaxRate() {
    return getDouble("DEFAULT_TAX_RATE", 12.00);
  }

  public static String taxType() {
    return get("TAX_TYPE", "Inclusive of Tax");
  }

  public static int priceDecimalPlaces() {
    return getInt("PRICE_DECIMAL_PLACES", 2);
  }

  // ─── File Storage ───────────────────────────────────────────────────────────

  public static String fileStorageRoot() {
    return get("FILE_STORAGE_ROOT", "./storage");
  }

  public static String uploadDir() {
    return get("UPLOAD_DIR", "./storage/uploads");
  }

  public static String inventoryProofUploadDir() {
    return get("INVENTORY_PROOF_UPLOAD_DIR", "./storage/uploads/inventory-change-requests");
  }

  public static String exportDir() {
    return get("EXPORT_DIR", "./storage/exports");
  }

  public static String backupDir() {
    return get("BACKUP_DIR", "./storage/backups");
  }

  public static String tempDir() {
    return get("TEMP_DIR", "./storage/tmp");
  }

  public static List<String> uploadAllowedMimeTypes() {
    return getList("UPLOAD_ALLOWED_MIME_TYPES", "image/jpeg,image/png");
  }

  public static List<String> uploadAllowedExtensions() {
    return getList("UPLOAD_ALLOWED_EXTENSIONS", "jpg,jpeg,png");
  }

  public static int uploadMaxImageBytes() {
    return getInt("UPLOAD_MAX_IMAGE_BYTES", 5242880);
  }

  // ─── Reports / Export ───────────────────────────────────────────────────────

  public static String exportDefaultFormat() {
    return get("EXPORT_DEFAULT_FORMAT", "csv");
  }

  public static List<String> exportAllowedFormats() {
    return getList("EXPORT_ALLOWED_FORMATS", "csv,xlsx,pdf");
  }

  public static int reportDefaultPageSize() {
    return getInt("REPORT_DEFAULT_PAGE_SIZE", 10);
  }

  public static int reportMaxPageSize() {
    return getInt("REPORT_MAX_PAGE_SIZE", 1000);
  }

  public static boolean pdfExportEnabled() {
    return getBool("PDF_EXPORT_ENABLED", true);
  }

  public static boolean excelExportEnabled() {
    return getBool("EXCEL_EXPORT_ENABLED", true);
  }

  public static boolean csvExportEnabled() {
    return getBool("CSV_EXPORT_ENABLED", true);
  }

  // ─── Email / Notifications ──────────────────────────────────────────────────

  public static boolean emailNotificationsEnabled() {
    return getBool("EMAIL_NOTIFICATIONS_ENABLED", false);
  }

  public static String smtpHost() {
    return get("SMTP_HOST", "smtp.gmail.com");
  }

  public static int smtpPort() {
    return getInt("SMTP_PORT", 587);
  }

  public static String smtpUsername() {
    return get("SMTP_USERNAME", "");
  }

  public static String smtpPassword() {
    return get("SMTP_PASSWORD", "");
  }

  public static boolean smtpUseSsl() {
    return getBool("SMTP_USE_SSL", true);
  }

  public static boolean smtpUseStarttls() {
    return getBool("SMTP_USE_STARTTLS", true);
  }

  public static String emailFrom() {
    return get("EMAIL_FROM", "noreply@abctrading.com");
  }

  public static boolean notifyLowStockAlerts() {
    return getBool("NOTIFY_LOW_STOCK_ALERTS", true);
  }

  public static boolean notifyDailySummaryReports() {
    return getBool("NOTIFY_DAILY_SUMMARY_REPORTS", false);
  }

  public static boolean notifyBackupStatus() {
    return getBool("NOTIFY_BACKUP_STATUS", true);
  }

  // ─── Backup / Restore ───────────────────────────────────────────────────────

  public static boolean backupEnabled() {
    return getBool("BACKUP_ENABLED", true);
  }

  public static String backupFrequency() {
    return get("BACKUP_FREQUENCY", "Daily");
  }

  public static String backupTime() {
    return get("BACKUP_TIME", "02:00");
  }

  public static boolean backupOnExit() {
    return getBool("BACKUP_ON_EXIT", true);
  }

  public static int backupRetentionDays() {
    return getInt("BACKUP_RETENTION_DAYS", 30);
  }

  public static String mysqldumpBin() {
    return get("MYSQLDUMP_BIN", "mysqldump");
  }

  public static String mysqlBin() {
    return get("MYSQL_BIN", "mysql");
  }

  // ─── CORS / API Access ──────────────────────────────────────────────────────

  public static boolean corsEnabled() {
    return getBool("CORS_ENABLED", true);
  }

  public static List<String> corsAllowedOrigins() {
    return getList("CORS_ALLOWED_ORIGINS", "http://localhost:3000,http://localhost:8080");
  }

  public static List<String> corsAllowedMethods() {
    return getList("CORS_ALLOWED_METHODS", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
  }

  public static List<String> corsAllowedHeaders() {
    return getList("CORS_ALLOWED_HEADERS", "Authorization,Content-Type,Accept");
  }

  // ─── Logging / Audit ────────────────────────────────────────────────────────

  public static String logLevel() {
    return get("LOG_LEVEL", "INFO");
  }

  public static String logDir() {
    return get("LOG_DIR", "./logs");
  }

  public static String logFile() {
    return get("LOG_FILE", "./logs/inventory-api.log");
  }

  public static int logMaxFileSizeMb() {
    return getInt("LOG_MAX_FILE_SIZE_MB", 10);
  }

  public static int logMaxHistoryDays() {
    return getInt("LOG_MAX_HISTORY_DAYS", 14);
  }

  public static boolean auditLogEnabled() {
    return getBool("AUDIT_LOG_ENABLED", true);
  }

  public static boolean auditLogRequestBody() {
    return getBool("AUDIT_LOG_REQUEST_BODY", false);
  }

  public static boolean auditLogResponseBody() {
    return getBool("AUDIT_LOG_RESPONSE_BODY", false);
  }

  // ─── Swagger / API Docs ─────────────────────────────────────────────────────

  public static boolean swaggerEnabled() {
    return getBool("SWAGGER_ENABLED", true);
  }

  public static String swaggerOpenapiFile() {
    return get("SWAGGER_OPENAPI_FILE", "./docs/inventory_system_group4_openapi.yaml");
  }

  // ─── Development ────────────────────────────────────────────────────────────

  public static boolean devShowStacktrace() {
    return getBool("DEV_SHOW_STACKTRACE", true);
  }

  public static boolean devEnableDemoData() {
    return getBool("DEV_ENABLE_DEMO_DATA", false);
  }

  // ─── Helper Methods ─────────────────────────────────────────────────────────

  private static String get(String key, String defaultValue) {
    String value = System.getenv(PREFIX + key);
    return (value != null && !value.isEmpty()) ? value : defaultValue;
  }

  private static int getInt(String key, int defaultValue) {
    String value = get(key, null);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private static long getLong(String key, long defaultValue) {
    String value = get(key, null);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private static double getDouble(String key, double defaultValue) {
    String value = get(key, null);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private static boolean getBool(String key, boolean defaultValue) {
    String value = get(key, null);
    if (value == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(value.trim());
  }

  private static List<String> getList(String key, String defaultValue) {
    String value = get(key, defaultValue);
    if (value == null || value.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(Arrays.asList(value.split(",")));
  }
}
