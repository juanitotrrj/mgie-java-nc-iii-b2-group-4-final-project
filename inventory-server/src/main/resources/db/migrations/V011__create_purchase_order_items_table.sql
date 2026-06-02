CREATE TABLE IF NOT EXISTS purchase_order_items (
  purchase_order_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  purchase_order_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  quantity INT NOT NULL,
  unit_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  line_total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (purchase_order_item_id),
  UNIQUE KEY uk_purchase_order_items_order_product (purchase_order_id, product_id),
  KEY idx_purchase_order_items_product_id (product_id),
  CONSTRAINT fk_purchase_order_items_order
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_purchase_order_items_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_purchase_order_items_quantity_positive CHECK (quantity > 0),
  CONSTRAINT chk_purchase_order_items_unit_cost_nonnegative CHECK (unit_cost >= 0),
  CONSTRAINT chk_purchase_order_items_line_total_nonnegative CHECK (line_total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
