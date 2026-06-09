CREATE TABLE IF NOT EXISTS purchase_orders (
  purchase_order_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  po_number VARCHAR(40) NOT NULL,
  supplier_id BIGINT UNSIGNED NOT NULL,
  order_date DATE NOT NULL,
  expected_delivery_date DATE NULL,
  received_date DATE NULL,
  status ENUM('Pending','Delivered','Received','Cancelled') NOT NULL DEFAULT 'Pending',
  subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  notes VARCHAR(1000) NULL,
  cancel_reason VARCHAR(1000) NULL,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  received_by BIGINT UNSIGNED NULL,
  cancelled_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (purchase_order_id),
  UNIQUE KEY uk_purchase_orders_po_number (po_number),
  KEY idx_purchase_orders_supplier_id (supplier_id),
  KEY idx_purchase_orders_status (status),
  KEY idx_purchase_orders_order_date (order_date),
  CONSTRAINT fk_purchase_orders_supplier
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_purchase_orders_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_purchase_orders_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_purchase_orders_received_by
    FOREIGN KEY (received_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_purchase_orders_cancelled_by
    FOREIGN KEY (cancelled_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_purchase_orders_total_nonnegative CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
