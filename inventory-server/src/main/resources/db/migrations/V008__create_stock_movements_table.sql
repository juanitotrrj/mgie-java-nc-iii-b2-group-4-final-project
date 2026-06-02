CREATE TABLE IF NOT EXISTS stock_movements (
  stock_movement_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  movement_type ENUM('OPENING','PURCHASE_IN','SALE_OUT','ADJUSTMENT_IN','ADJUSTMENT_OUT','SALE_REVERSAL','PURCHASE_REVERSAL') NOT NULL,
  reference_type ENUM('PRODUCT','PURCHASE','SALE','INVENTORY_CHANGE_REQUEST','SYSTEM') NOT NULL,
  reference_id BIGINT UNSIGNED NULL,
  reference_no VARCHAR(60) NULL,
  quantity_before INT NOT NULL,
  quantity_change INT NOT NULL,
  quantity_after INT NOT NULL,
  remarks VARCHAR(1000) NULL,
  created_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (stock_movement_id),
  KEY idx_stock_movements_product_id (product_id),
  KEY idx_stock_movements_movement_type (movement_type),
  KEY idx_stock_movements_reference (reference_type, reference_id),
  KEY idx_stock_movements_created_at (created_at),
  CONSTRAINT fk_stock_movements_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_stock_movements_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
