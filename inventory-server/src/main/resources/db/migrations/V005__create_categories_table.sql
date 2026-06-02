CREATE TABLE IF NOT EXISTS categories (
  category_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_code VARCHAR(30) NOT NULL,
  category_name VARCHAR(120) NOT NULL,
  description VARCHAR(500) NULL,
  category_type VARCHAR(80) NOT NULL DEFAULT 'Product Group',
  status ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (category_id),
  UNIQUE KEY uk_categories_code (category_code),
  UNIQUE KEY uk_categories_name (category_name),
  KEY idx_categories_status (status),
  KEY idx_categories_type (category_type),
  CONSTRAINT fk_categories_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_categories_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
