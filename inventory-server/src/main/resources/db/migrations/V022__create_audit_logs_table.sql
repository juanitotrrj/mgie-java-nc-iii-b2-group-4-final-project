CREATE TABLE IF NOT EXISTS audit_logs (
  audit_log_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NULL,
  username VARCHAR(100),
  action VARCHAR(100) NOT NULL,
  module VARCHAR(100) NOT NULL,
  reference_no VARCHAR(100),
  details TEXT,
  ip_address VARCHAR(50),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (audit_log_id),
  CONSTRAINT fk_audit_logs_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  INDEX idx_audit_logs_created_at (created_at),
  INDEX idx_audit_logs_user_id (user_id),
  INDEX idx_audit_logs_action (action),
  INDEX idx_audit_logs_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
