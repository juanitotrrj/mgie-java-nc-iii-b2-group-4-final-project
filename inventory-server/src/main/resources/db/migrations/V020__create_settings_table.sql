CREATE TABLE IF NOT EXISTS settings (
  setting_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  setting_group VARCHAR(50) NOT NULL,
  setting_key VARCHAR(100) NOT NULL,
  setting_value TEXT,
  data_type ENUM('string','integer','decimal','boolean','json') NOT NULL DEFAULT 'string',
  description VARCHAR(500),
  is_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
  updated_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (setting_id),
  UNIQUE KEY uk_settings_group_key (setting_group, setting_key),
  CONSTRAINT fk_settings_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default settings
INSERT INTO settings (setting_group, setting_key, setting_value, data_type, description) VALUES
-- Company
('company', 'company_name', 'My Company', 'string', 'Business name'),
('company', 'address', '', 'string', 'Business address'),
('company', 'contact_number', '', 'string', 'Contact phone number'),
('company', 'email_address', '', 'string', 'Business email address'),
('company', 'website', '', 'string', 'Company website URL'),
('company', 'fiscal_year_start', '01-01', 'string', 'Fiscal year start (MM-DD)'),
-- Inventory
('inventory', 'low_stock_threshold', '10', 'integer', 'Default low stock threshold'),
('inventory', 'reorder_multiplier', '2', 'decimal', 'Reorder quantity multiplier'),
('inventory', 'default_product_status', 'In Stock', 'string', 'Default status for new products'),
('inventory', 'costing_method', 'Average Cost', 'string', 'Inventory costing method'),
('inventory', 'allow_negative_stock', 'false', 'boolean', 'Allow stock to go negative'),
('inventory', 'show_delete_confirmation', 'true', 'boolean', 'Show confirmation on delete'),
('inventory', 'auto_update_total_values', 'true', 'boolean', 'Auto-recalculate totals'),
('inventory', 'warn_when_stock_falls_below_threshold', 'true', 'boolean', 'Warn on low stock'),
('inventory', 'track_product_expiration_dates', 'false', 'boolean', 'Track expiry dates'),
('inventory', 'default_expiry_warning_days', '30', 'integer', 'Days before expiry to warn'),
-- Currency & Tax
('currency_tax', 'currency', 'PHP', 'string', 'Currency code'),
('currency_tax', 'currency_symbol', '₱', 'string', 'Currency symbol'),
('currency_tax', 'default_tax_rate', '12', 'decimal', 'Default tax rate percentage'),
('currency_tax', 'tax_type', 'Inclusive of Tax', 'string', 'Tax calculation type'),
('currency_tax', 'rounding', '2', 'integer', 'Decimal places for rounding'),
-- Security
('security', 'session_timeout_minutes', '30', 'integer', 'Session timeout in minutes'),
('security', 'password_policy', 'Standard', 'string', 'Password policy level'),
('security', 'require_login_on_startup', 'true', 'boolean', 'Require login on app start'),
('security', 'allow_multiple_concurrent_sessions', 'false', 'boolean', 'Allow multiple sessions'),
('security', 'lock_account_after_failed_attempts', '5', 'integer', 'Failed attempts before lockout'),
('security', 'require_password_change_every_days', '90', 'integer', 'Password rotation interval'),
('security', 'minimum_password_length', '8', 'integer', 'Minimum password length'),
('security', 'password_expiry_days', '90', 'integer', 'Days until password expires'),
-- Notifications
('notifications', 'enable_email_notifications', 'false', 'boolean', 'Enable email notifications'),
('notifications', 'smtp_server', '', 'string', 'SMTP server hostname'),
('notifications', 'port', '587', 'integer', 'SMTP port'),
('notifications', 'use_ssl', 'true', 'boolean', 'Use SSL for SMTP'),
('notifications', 'from_email', '', 'string', 'From email address'),
('notifications', 'low_stock_alerts', 'true', 'boolean', 'Send low stock alerts'),
('notifications', 'daily_summary_reports', 'false', 'boolean', 'Send daily summary emails'),
('notifications', 'backup_status_notifications', 'true', 'boolean', 'Send backup status alerts'),
-- Database
('database', 'database_type', 'MySQL', 'string', 'Database engine type'),
('database', 'database_file', '', 'string', 'Database file path (if applicable)'),
('database', 'auto_backup', 'true', 'boolean', 'Enable automatic backups'),
('database', 'backup_on_exit', 'false', 'boolean', 'Backup on application exit'),
('database', 'backup_frequency', 'Daily', 'string', 'Automatic backup frequency'),
('database', 'backup_time', '02:00', 'string', 'Scheduled backup time');
