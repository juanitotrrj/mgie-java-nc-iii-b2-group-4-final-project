# ============================================================================
# G4IMS Server - Environment Variable Registration (Windows PowerShell)
# ============================================================================
# Run with: powershell -ExecutionPolicy Bypass -File .\register-server-env-windows.ps1
#
# This registers all G4IMS_SERVER_* environment variables at the User level.
# After running, restart PowerShell, your IDE, or the Java application.
# ============================================================================

Write-Host "=== G4IMS Server Environment Registration (Windows) ===" -ForegroundColor Cyan
Write-Host ""

# ─── Application / Runtime ──────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_APP_NAME", "Inventory Management System - Group 4 API", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_APP_ENV", "development", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_APP_VERSION", "1.0.0", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_APP_TIMEZONE", "UTC", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_APP_BASE_URL", "http://localhost:8080", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_APP_CONTEXT_PATH", "/api", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SERVER_HOST", "0.0.0.0", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SERVER_PORT", "8080", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SERVER_BACKLOG", "100", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SERVER_MAX_REQUEST_BODY_BYTES", "10485760", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SERVER_MAX_UPLOAD_BYTES", "5242880", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SERVER_REQUEST_TIMEOUT_SECONDS", "30", "User")

# ─── Database ───────────────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_DRIVER", "com.mysql.cj.jdbc.Driver", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_HOST", "localhost", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_PORT", "3306", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_NAME", "inventory_system_group4", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_USER", "inventory_user", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_PASSWORD", "change_me_secure_password", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_URL", "jdbc:mysql://localhost:3306/inventory_system_group4?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_POOL_MIN_SIZE", "2", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_POOL_MAX_SIZE", "10", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_POOL_CONNECTION_TIMEOUT_MS", "30000", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_POOL_IDLE_TIMEOUT_MS", "600000", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_POOL_MAX_LIFETIME_MS", "1800000", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_RUN_MIGRATIONS_ON_STARTUP", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_RUN_SEED_ON_STARTUP", "false", "User")

# ─── Authentication / Security ──────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUTH_TOKEN_TYPE", "Bearer", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUTH_TOKEN_SECRET", "replace_with_long_random_secret_at_least_32_chars", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUTH_TOKEN_EXPIRY_SECONDS", "1800", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUTH_REFRESH_TOKEN_EXPIRY_SECONDS", "86400", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_HASH_ALGORITHM", "BCrypt", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_BCRYPT_COST", "10", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_MIN_LENGTH", "8", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_REQUIRE_UPPERCASE", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_REQUIRE_LOWERCASE", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_REQUIRE_NUMBER", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PASSWORD_REQUIRE_SYMBOL", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SECURITY_SESSION_TIMEOUT_MINUTES", "30", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SECURITY_ALLOW_MULTIPLE_SESSIONS", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SECURITY_LOCK_AFTER_FAILED_ATTEMPTS", "5", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SECURITY_PASSWORD_EXPIRY_DAYS", "90", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SECURITY_REQUIRE_LOGIN_ON_STARTUP", "true", "User")

# ─── Default Admin Bootstrap ────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEFAULT_ADMIN_USERNAME", "admin", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEFAULT_ADMIN_PASSWORD", "Admin@123", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEFAULT_ADMIN_EMAIL", "admin@inventory.local", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEFAULT_ADMIN_FULL_NAME", "System Administrator", "User")

# ─── Business Rules ─────────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_LOW_STOCK_THRESHOLD", "10", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_REORDER_MULTIPLIER", "1.50", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_DEFAULT_PRODUCT_STATUS", "In Stock", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_COSTING_METHOD", "Average Cost", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_ALLOW_NEGATIVE_STOCK", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_AUTO_UPDATE_TOTAL_VALUES", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_WARN_BELOW_THRESHOLD", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_TRACK_EXPIRATION_DATES", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_DEFAULT_EXPIRY_WARNING_DAYS", "30", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_CLERK_CHANGES_REQUIRE_MANAGER_APPROVAL", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SALES_ALLOW_PENDING_INVOICE", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SALES_ALLOW_CANCEL_PAID_INVOICE", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PURCHASE_ALLOW_RECEIVE_PENDING_ONLY", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PURCHASE_ALLOW_CANCEL_RECEIVED", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CURRENCY_NAME", "Philippine Peso", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CURRENCY_CODE", "PHP", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CURRENCY_SYMBOL", "`u{20B1}", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEFAULT_TAX_RATE", "12.00", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_TAX_TYPE", "Inclusive of Tax", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PRICE_DECIMAL_PLACES", "2", "User")

# ─── File Storage ───────────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_FILE_STORAGE_ROOT", "./storage", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_UPLOAD_DIR", "./storage/uploads", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_INVENTORY_PROOF_UPLOAD_DIR", "./storage/uploads/inventory-change-requests", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_EXPORT_DIR", "./storage/exports", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_BACKUP_DIR", "./storage/backups", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_TEMP_DIR", "./storage/tmp", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_UPLOAD_ALLOWED_MIME_TYPES", "image/jpeg,image/png", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_UPLOAD_ALLOWED_EXTENSIONS", "jpg,jpeg,png", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_UPLOAD_MAX_IMAGE_BYTES", "5242880", "User")

# ─── Reports / Export ───────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_EXPORT_DEFAULT_FORMAT", "csv", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_EXPORT_ALLOWED_FORMATS", "csv,xlsx,pdf", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_REPORT_DEFAULT_PAGE_SIZE", "10", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_REPORT_MAX_PAGE_SIZE", "1000", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_PDF_EXPORT_ENABLED", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_EXCEL_EXPORT_ENABLED", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CSV_EXPORT_ENABLED", "true", "User")

# ─── Email / Notifications ──────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_EMAIL_NOTIFICATIONS_ENABLED", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SMTP_HOST", "smtp.gmail.com", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SMTP_PORT", "587", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SMTP_USERNAME", "", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SMTP_PASSWORD", "", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SMTP_USE_SSL", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SMTP_USE_STARTTLS", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_EMAIL_FROM", "noreply@abctrading.com", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_NOTIFY_LOW_STOCK_ALERTS", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_NOTIFY_DAILY_SUMMARY_REPORTS", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_NOTIFY_BACKUP_STATUS", "true", "User")

# ─── Backup / Restore ───────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_BACKUP_ENABLED", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_BACKUP_FREQUENCY", "Daily", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_BACKUP_TIME", "02:00", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_BACKUP_ON_EXIT", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_BACKUP_RETENTION_DAYS", "30", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_MYSQLDUMP_BIN", "mysqldump", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_MYSQL_BIN", "mysql", "User")

# ─── CORS / API Access ──────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CORS_ENABLED", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CORS_ALLOWED_ORIGINS", "http://localhost:3000,http://localhost:8080", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CORS_ALLOWED_METHODS", "GET,POST,PUT,PATCH,DELETE,OPTIONS", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_CORS_ALLOWED_HEADERS", "Authorization,Content-Type,Accept", "User")

# ─── Logging / Audit ────────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_LOG_LEVEL", "INFO", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_LOG_DIR", "./logs", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_LOG_FILE", "./logs/inventory-api.log", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_LOG_MAX_FILE_SIZE_MB", "10", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_LOG_MAX_HISTORY_DAYS", "14", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUDIT_LOG_ENABLED", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUDIT_LOG_REQUEST_BODY", "false", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUDIT_LOG_RESPONSE_BODY", "false", "User")

# ─── Swagger / API Docs ─────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SWAGGER_ENABLED", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_SWAGGER_OPENAPI_FILE", "./docs/inventory_system_group4_openapi.yaml", "User")

# ─── Development ────────────────────────────────────────────────────────────
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEV_SHOW_STACKTRACE", "true", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DEV_ENABLE_DEMO_DATA", "false", "User")

Write-Host ""
Write-Host "=== Done! ===" -ForegroundColor Green
Write-Host "Registered 116 environment variables at the User level."
Write-Host ""
Write-Host "IMPORTANT: Restart PowerShell, your IDE, or the Java application." -ForegroundColor Yellow
Write-Host ""
Write-Host "SECURITY REMINDER:" -ForegroundColor Red
Write-Host "  - Change G4IMS_SERVER_DB_PASSWORD to a real password"
Write-Host "  - Change G4IMS_SERVER_AUTH_TOKEN_SECRET to a 32+ char random string"
Write-Host "  - Change G4IMS_SERVER_DEFAULT_ADMIN_PASSWORD before production"
