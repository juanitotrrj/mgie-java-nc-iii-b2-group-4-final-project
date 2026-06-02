CREATE TABLE IF NOT EXISTS inventory_change_request_files (
  file_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  inventory_change_request_id BIGINT UNSIGNED NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  stored_filename VARCHAR(255) NOT NULL,
  storage_path VARCHAR(500) NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  file_size_bytes BIGINT UNSIGNED NOT NULL,
  uploaded_by BIGINT UNSIGNED NOT NULL,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (file_id),
  KEY idx_icrf_request_id (inventory_change_request_id),
  CONSTRAINT fk_icrf_request
    FOREIGN KEY (inventory_change_request_id) REFERENCES inventory_change_requests(inventory_change_request_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_icrf_uploaded_by
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_icrf_file_size CHECK (file_size_bytes > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
