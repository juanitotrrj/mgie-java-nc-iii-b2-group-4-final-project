CREATE TABLE IF NOT EXISTS sales (
  sale_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  invoice_no VARCHAR(40) NOT NULL,
  customer_name VARCHAR(150) NOT NULL DEFAULT 'Walk-in Customer',
  cashier_id BIGINT UNSIGNED NULL,
  sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  payment_method ENUM('Cash','GCash','Credit Card','Bank Transfer','E-Wallet') NOT NULL DEFAULT 'Cash',
  status ENUM('Pending','Paid','Cancelled') NOT NULL DEFAULT 'Paid',
  subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  amount_received DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  change_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  cancel_reason VARCHAR(1000) NULL,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  cancelled_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (sale_id),
  UNIQUE KEY uk_sales_invoice_no (invoice_no),
  KEY idx_sales_cashier_id (cashier_id),
  KEY idx_sales_sale_date (sale_date),
  KEY idx_sales_status (status),
  KEY idx_sales_payment_method (payment_method),
  CONSTRAINT fk_sales_cashier
    FOREIGN KEY (cashier_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_sales_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_sales_updated_by
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_sales_cancelled_by
    FOREIGN KEY (cancelled_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_sales_total_nonnegative CHECK (total_amount >= 0),
  CONSTRAINT chk_sales_amount_received_nonnegative CHECK (amount_received >= 0),
  CONSTRAINT chk_sales_change_nonnegative CHECK (change_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
