CREATE TABLE IF NOT EXISTS suppliers (
  supplier_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  supplier_code VARCHAR(30) NOT NULL,
  supplier_name VARCHAR(150) NOT NULL,
  contact_person VARCHAR(150) NULL,
  phone VARCHAR(60) NULL,
  email VARCHAR(150) NULL,
  address VARCHAR(500) NULL,
  supplier_type ENUM('Local','International','Preferred','Other') NOT NULL DEFAULT 'Local',
  preferred TINYINT(1) NOT NULL DEFAULT 0,
  status ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (supplier_id),
  UNIQUE KEY uk_suppliers_code (supplier_code),
  KEY idx_suppliers_name (supplier_name),
  KEY idx_suppliers_status (status),
  KEY idx_suppliers_type (supplier_type),
  KEY idx_suppliers_preferred (preferred),
  CONSTRAINT fk_suppliers_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_suppliers_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
