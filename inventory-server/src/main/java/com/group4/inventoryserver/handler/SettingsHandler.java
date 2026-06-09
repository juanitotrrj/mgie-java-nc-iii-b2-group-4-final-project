package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.settings.BackupResult;
import com.group4.inventoryserver.dto.settings.CompanySettings;
import com.group4.inventoryserver.dto.settings.CurrencyTaxSettings;
import com.group4.inventoryserver.dto.settings.DatabaseSettings;
import com.group4.inventoryserver.dto.settings.DatabaseTestResult;
import com.group4.inventoryserver.dto.settings.InventorySettings;
import com.group4.inventoryserver.dto.settings.NotificationSettings;
import com.group4.inventoryserver.dto.settings.RestoreRequest;
import com.group4.inventoryserver.dto.settings.SecuritySettings;
import com.group4.inventoryserver.dto.settings.SystemSettings;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.DatabaseBackupRepository;
import com.group4.inventoryserver.repository.SettingsRepository;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.util.DatabaseBackupUtil;
import com.group4.inventoryserver.util.DateUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsHandler extends BaseHandler {

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/settings";
  private static final String PERM_SETTINGS = "SETTINGS_MANAGE";
  private static final String PERM_BACKUP = "BACKUP_MANAGE";

  private final SettingsRepository settingsRepository = new SettingsRepository();
  private final DatabaseBackupRepository backupRepository = new DatabaseBackupRepository();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      requirePermission(ctx, PERM_SETTINGS);
      if (!"GET".equals(method)) {
        throw new ApiException(405, "Method Not Allowed: " + method);
      }
      handleGetAll(ctx);
      return;
    }

    switch (pathParam) {
      case "company":
        requirePermission(ctx, PERM_SETTINGS);
        requireMethod(method, "PUT");
        handleUpdateCompany(ctx);
        break;
      case "inventory":
        requirePermission(ctx, PERM_SETTINGS);
        requireMethod(method, "PUT");
        handleUpdateInventory(ctx);
        break;
      case "currency-tax":
        requirePermission(ctx, PERM_SETTINGS);
        requireMethod(method, "PUT");
        handleUpdateCurrencyTax(ctx);
        break;
      case "security":
        requirePermission(ctx, PERM_SETTINGS);
        requireMethod(method, "PUT");
        handleUpdateSecurity(ctx);
        break;
      case "notifications":
        requirePermission(ctx, PERM_SETTINGS);
        requireMethod(method, "PUT");
        handleUpdateNotifications(ctx);
        break;
      case "database/test":
        requirePermission(ctx, PERM_BACKUP);
        requireMethod(method, "GET");
        handleDatabaseTest(ctx);
        break;
      case "database/backup":
        requirePermission(ctx, PERM_BACKUP);
        requireMethod(method, "POST");
        handleDatabaseBackup(ctx);
        break;
      case "database/restore":
        requirePermission(ctx, PERM_BACKUP);
        requireMethod(method, "POST");
        handleDatabaseRestore(ctx);
        break;
      default:
        throw new ApiException(404, "Not found.");
    }
  }

  private void handleGetAll(RequestContext ctx) throws IOException {
    SystemSettings settings = buildSystemSettings();
    sendSuccess(ctx, settings);
  }

  private void handleUpdateCompany(RequestContext ctx) throws IOException {
    CompanySettings req = parseBody(ctx, CompanySettings.class);
    long userId = getAuthUserId(ctx);
    Map<String, String> entries = new LinkedHashMap<>();
    if (req.getCompanyName() != null) entries.put("company_name", req.getCompanyName());
    if (req.getAddress() != null) entries.put("address", req.getAddress());
    if (req.getContactNumber() != null) entries.put("contact_number", req.getContactNumber());
    if (req.getEmailAddress() != null) entries.put("email_address", req.getEmailAddress());
    if (req.getWebsite() != null) entries.put("website", req.getWebsite());
    if (req.getFiscalYearStart() != null)
      entries.put("fiscal_year_start", req.getFiscalYearStart());
    if (!entries.isEmpty()) {
      settingsRepository.upsertBatch("company", entries, userId);
    }
    sendSuccess(ctx, buildSystemSettings());
  }

  private void handleUpdateInventory(RequestContext ctx) throws IOException {
    InventorySettings req = parseBody(ctx, InventorySettings.class);
    long userId = getAuthUserId(ctx);
    Map<String, String> entries = new LinkedHashMap<>();
    if (req.getLowStockThreshold() != null)
      entries.put("low_stock_threshold", String.valueOf(req.getLowStockThreshold()));
    if (req.getReorderMultiplier() != null)
      entries.put("reorder_multiplier", String.valueOf(req.getReorderMultiplier()));
    if (req.getDefaultProductStatus() != null)
      entries.put("default_product_status", req.getDefaultProductStatus());
    if (req.getCostingMethod() != null) entries.put("costing_method", req.getCostingMethod());
    if (req.getAllowNegativeStock() != null)
      entries.put("allow_negative_stock", String.valueOf(req.getAllowNegativeStock()));
    if (req.getShowDeleteConfirmation() != null)
      entries.put("show_delete_confirmation", String.valueOf(req.getShowDeleteConfirmation()));
    if (req.getAutoUpdateTotalValues() != null)
      entries.put("auto_update_total_values", String.valueOf(req.getAutoUpdateTotalValues()));
    if (req.getWarnWhenStockFallsBelowThreshold() != null)
      entries.put(
          "warn_when_stock_falls_below_threshold",
          String.valueOf(req.getWarnWhenStockFallsBelowThreshold()));
    if (req.getTrackProductExpirationDates() != null)
      entries.put(
          "track_product_expiration_dates", String.valueOf(req.getTrackProductExpirationDates()));
    if (req.getDefaultExpiryWarningDays() != null)
      entries.put("default_expiry_warning_days", String.valueOf(req.getDefaultExpiryWarningDays()));
    if (!entries.isEmpty()) {
      settingsRepository.upsertBatch("inventory", entries, userId);
    }
    sendSuccess(ctx, buildSystemSettings());
  }

  private void handleUpdateCurrencyTax(RequestContext ctx) throws IOException {
    CurrencyTaxSettings req = parseBody(ctx, CurrencyTaxSettings.class);
    long userId = getAuthUserId(ctx);
    Map<String, String> entries = new LinkedHashMap<>();
    if (req.getCurrency() != null) entries.put("currency", req.getCurrency());
    if (req.getCurrencySymbol() != null) entries.put("currency_symbol", req.getCurrencySymbol());
    if (req.getDefaultTaxRate() != null)
      entries.put("default_tax_rate", String.valueOf(req.getDefaultTaxRate()));
    if (req.getTaxType() != null) entries.put("tax_type", req.getTaxType());
    if (req.getRounding() != null) entries.put("rounding", String.valueOf(req.getRounding()));
    if (!entries.isEmpty()) {
      settingsRepository.upsertBatch("currency_tax", entries, userId);
    }
    sendSuccess(ctx, buildSystemSettings());
  }

  private void handleUpdateSecurity(RequestContext ctx) throws IOException {
    SecuritySettings req = parseBody(ctx, SecuritySettings.class);
    long userId = getAuthUserId(ctx);
    Map<String, String> entries = new LinkedHashMap<>();
    if (req.getSessionTimeoutMinutes() != null)
      entries.put("session_timeout_minutes", String.valueOf(req.getSessionTimeoutMinutes()));
    if (req.getPasswordPolicy() != null) entries.put("password_policy", req.getPasswordPolicy());
    if (req.getRequireLoginOnStartup() != null)
      entries.put("require_login_on_startup", String.valueOf(req.getRequireLoginOnStartup()));
    if (req.getAllowMultipleConcurrentSessions() != null)
      entries.put(
          "allow_multiple_concurrent_sessions",
          String.valueOf(req.getAllowMultipleConcurrentSessions()));
    if (req.getLockAccountAfterFailedAttempts() != null)
      entries.put(
          "lock_account_after_failed_attempts",
          String.valueOf(req.getLockAccountAfterFailedAttempts()));
    if (req.getRequirePasswordChangeEveryDays() != null)
      entries.put(
          "require_password_change_every_days",
          String.valueOf(req.getRequirePasswordChangeEveryDays()));
    if (req.getMinimumPasswordLength() != null)
      entries.put("minimum_password_length", String.valueOf(req.getMinimumPasswordLength()));
    if (req.getPasswordExpiryDays() != null)
      entries.put("password_expiry_days", String.valueOf(req.getPasswordExpiryDays()));
    if (!entries.isEmpty()) {
      settingsRepository.upsertBatch("security", entries, userId);
    }
    sendSuccess(ctx, buildSystemSettings());
  }

  private void handleUpdateNotifications(RequestContext ctx) throws IOException {
    NotificationSettings req = parseBody(ctx, NotificationSettings.class);
    long userId = getAuthUserId(ctx);
    Map<String, String> entries = new LinkedHashMap<>();
    if (req.getEnableEmailNotifications() != null)
      entries.put("enable_email_notifications", String.valueOf(req.getEnableEmailNotifications()));
    if (req.getSmtpServer() != null) entries.put("smtp_server", req.getSmtpServer());
    if (req.getPort() != null) entries.put("port", String.valueOf(req.getPort()));
    if (req.getUseSsl() != null) entries.put("use_ssl", String.valueOf(req.getUseSsl()));
    if (req.getFromEmail() != null) entries.put("from_email", req.getFromEmail());
    if (req.getLowStockAlerts() != null)
      entries.put("low_stock_alerts", String.valueOf(req.getLowStockAlerts()));
    if (req.getDailySummaryReports() != null)
      entries.put("daily_summary_reports", String.valueOf(req.getDailySummaryReports()));
    if (req.getBackupStatusNotifications() != null)
      entries.put(
          "backup_status_notifications", String.valueOf(req.getBackupStatusNotifications()));
    if (!entries.isEmpty()) {
      settingsRepository.upsertBatch("notifications", entries, userId);
    }
    sendSuccess(ctx, buildSystemSettings());
  }

  private void handleDatabaseTest(RequestContext ctx) throws IOException {
    long start = System.currentTimeMillis();
    String status = "Connected";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT 1");
        ResultSet rs = ps.executeQuery()) {
      rs.next();
    } catch (Exception e) {
      status = "Failed: " + e.getMessage();
    }
    long elapsed = System.currentTimeMillis() - start;
    DatabaseTestResult result =
        new DatabaseTestResult(status, "MySQL", EnvConfig.dbName(), elapsed);
    sendSuccess(ctx, result);
  }

  private void handleDatabaseBackup(RequestContext ctx) throws IOException {
    long userId = getAuthUserId(ctx);
    DatabaseBackupUtil.BackupInfo info = DatabaseBackupUtil.backup();
    backupRepository.insert(
        info.getFilename(), info.getPath(), info.getSizeBytes(), "Created", userId);
    BackupResult result =
        new BackupResult(
            info.getFilename(),
            DateUtil.formatIso(new java.sql.Timestamp(System.currentTimeMillis())));
    sendCreated(ctx, result);
  }

  private void handleDatabaseRestore(RequestContext ctx) throws IOException {
    RestoreRequest req = parseBody(ctx, RestoreRequest.class);
    if (req.getBackupFile() == null || req.getBackupFile().trim().isEmpty()) {
      throw new ValidationException("Backup file name is required.");
    }
    long userId = getAuthUserId(ctx);
    String path = backupRepository.findPathByFilename(req.getBackupFile().trim());
    if (path == null) {
      throw new NotFoundException("Backup file not found: " + req.getBackupFile());
    }
    DatabaseBackupUtil.restore(path);
    backupRepository.markRestored(req.getBackupFile().trim(), userId);
    sendSuccess(ctx, "Database restored successfully from: " + req.getBackupFile());
  }

  // --- Helpers ---

  private SystemSettings buildSystemSettings() {
    Map<String, Map<String, String>> allSettings = settingsRepository.findAll();
    SystemSettings sys = new SystemSettings();

    Map<String, String> company = allSettings.getOrDefault("company", new LinkedHashMap<>());
    CompanySettings cs = new CompanySettings();
    cs.setCompanyName(company.get("company_name"));
    cs.setAddress(company.get("address"));
    cs.setContactNumber(company.get("contact_number"));
    cs.setEmailAddress(company.get("email_address"));
    cs.setWebsite(company.get("website"));
    cs.setFiscalYearStart(company.get("fiscal_year_start"));
    sys.setCompany(cs);

    Map<String, String> inv = allSettings.getOrDefault("inventory", new LinkedHashMap<>());
    InventorySettings is = new InventorySettings();
    is.setLowStockThreshold(parseInt(inv.get("low_stock_threshold")));
    is.setReorderMultiplier(parseDouble(inv.get("reorder_multiplier")));
    is.setDefaultProductStatus(inv.get("default_product_status"));
    is.setCostingMethod(inv.get("costing_method"));
    is.setAllowNegativeStock(parseBool(inv.get("allow_negative_stock")));
    is.setShowDeleteConfirmation(parseBool(inv.get("show_delete_confirmation")));
    is.setAutoUpdateTotalValues(parseBool(inv.get("auto_update_total_values")));
    is.setWarnWhenStockFallsBelowThreshold(
        parseBool(inv.get("warn_when_stock_falls_below_threshold")));
    is.setTrackProductExpirationDates(parseBool(inv.get("track_product_expiration_dates")));
    is.setDefaultExpiryWarningDays(parseInt(inv.get("default_expiry_warning_days")));
    sys.setInventory(is);

    Map<String, String> ct = allSettings.getOrDefault("currency_tax", new LinkedHashMap<>());
    CurrencyTaxSettings cts = new CurrencyTaxSettings();
    cts.setCurrency(ct.get("currency"));
    cts.setCurrencySymbol(ct.get("currency_symbol"));
    cts.setDefaultTaxRate(parseDouble(ct.get("default_tax_rate")));
    cts.setTaxType(ct.get("tax_type"));
    cts.setRounding(parseInt(ct.get("rounding")));
    sys.setCurrencyTax(cts);

    Map<String, String> sec = allSettings.getOrDefault("security", new LinkedHashMap<>());
    SecuritySettings ss = new SecuritySettings();
    ss.setSessionTimeoutMinutes(parseInt(sec.get("session_timeout_minutes")));
    ss.setPasswordPolicy(sec.get("password_policy"));
    ss.setRequireLoginOnStartup(parseBool(sec.get("require_login_on_startup")));
    ss.setAllowMultipleConcurrentSessions(parseBool(sec.get("allow_multiple_concurrent_sessions")));
    ss.setLockAccountAfterFailedAttempts(parseInt(sec.get("lock_account_after_failed_attempts")));
    ss.setRequirePasswordChangeEveryDays(parseInt(sec.get("require_password_change_every_days")));
    ss.setMinimumPasswordLength(parseInt(sec.get("minimum_password_length")));
    ss.setPasswordExpiryDays(parseInt(sec.get("password_expiry_days")));
    sys.setSecurity(ss);

    Map<String, String> notif = allSettings.getOrDefault("notifications", new LinkedHashMap<>());
    NotificationSettings ns = new NotificationSettings();
    ns.setEnableEmailNotifications(parseBool(notif.get("enable_email_notifications")));
    ns.setSmtpServer(notif.get("smtp_server"));
    ns.setPort(parseInt(notif.get("port")));
    ns.setUseSsl(parseBool(notif.get("use_ssl")));
    ns.setFromEmail(notif.get("from_email"));
    ns.setLowStockAlerts(parseBool(notif.get("low_stock_alerts")));
    ns.setDailySummaryReports(parseBool(notif.get("daily_summary_reports")));
    ns.setBackupStatusNotifications(parseBool(notif.get("backup_status_notifications")));
    sys.setNotifications(ns);

    Map<String, String> db = allSettings.getOrDefault("database", new LinkedHashMap<>());
    DatabaseSettings ds = new DatabaseSettings();
    ds.setDatabaseType(db.get("database_type"));
    ds.setDatabaseFile(db.get("database_file"));
    ds.setAutoBackup(parseBool(db.get("auto_backup")));
    ds.setBackupOnExit(parseBool(db.get("backup_on_exit")));
    ds.setBackupFrequency(db.get("backup_frequency"));
    ds.setBackupTime(db.get("backup_time"));
    sys.setDatabase(ds);

    return sys;
  }

  private void requireMethod(String actual, String expected) {
    if (!expected.equals(actual)) {
      throw new ApiException(405, "Method Not Allowed: " + actual);
    }
  }

  private Integer parseInt(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Integer.valueOf(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Double parseDouble(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Double.valueOf(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Boolean parseBool(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
  }
}
