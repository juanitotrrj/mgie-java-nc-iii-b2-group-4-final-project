CREATE TABLE IF NOT EXISTS database_backups (
  database_backup_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  backup_filename VARCHAR(255) NOT NULL,
  backup_path VARCHAR(500) NOT NULL,
  file_size_bytes BIGINT UNSIGNED DEFAULT 0,
  status ENUM('Created','Restored','Failed') NOT NULL DEFAULT 'Created',
  notes TEXT,
  created_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  restored_by BIGINT UNSIGNED NULL,
  restored_at TIMESTAMP NULL,
  PRIMARY KEY (database_backup_id),
  CONSTRAINT fk_database_backups_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_database_backups_restored_by
    FOREIGN KEY (restored_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
