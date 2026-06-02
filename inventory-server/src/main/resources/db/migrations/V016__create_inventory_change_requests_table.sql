CREATE TABLE IF NOT EXISTS inventory_change_requests (
  inventory_change_request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  request_no VARCHAR(40) NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  request_type ENUM('Adjustment','Correction','Damage','Lost Item','Found Item') NOT NULL DEFAULT 'Adjustment',
  current_quantity INT NOT NULL,
  requested_quantity INT NULL,
  quantity_change INT NULL,
  reason VARCHAR(1000) NOT NULL,
  status ENUM('Pending','Approved','Rejected','Cancelled') NOT NULL DEFAULT 'Pending',
  requested_by BIGINT UNSIGNED NOT NULL,
  requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_by BIGINT UNSIGNED NULL,
  reviewed_at TIMESTAMP NULL,
  review_notes VARCHAR(1000) NULL,
  stock_movement_id BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (inventory_change_request_id),
  UNIQUE KEY uk_icr_request_no (request_no),
  KEY idx_icr_product_id (product_id),
  KEY idx_icr_status (status),
  KEY idx_icr_requested_by (requested_by),
  KEY idx_icr_requested_at (requested_at),
  CONSTRAINT fk_icr_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_icr_requested_by
    FOREIGN KEY (requested_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_icr_reviewed_by
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_icr_stock_movement
    FOREIGN KEY (stock_movement_id) REFERENCES stock_movements(stock_movement_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
