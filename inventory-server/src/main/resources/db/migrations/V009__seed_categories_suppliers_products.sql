INSERT IGNORE INTO categories (category_code, category_name, description, category_type, status)
VALUES
('CAT001', 'Accessories', 'Computer and device accessories', 'Product Group', 'Active'),
('CAT002', 'Electronics', 'Electronic equipment and devices', 'Product Group', 'Active'),
('CAT003', 'Office Supplies', 'Supplies used for office operations', 'Product Group', 'Active'),
('CAT004', 'Furniture', 'Office and store furniture', 'Product Group', 'Active');

INSERT IGNORE INTO suppliers (supplier_code, supplier_name, contact_person, phone, email, address, supplier_type, preferred, status)
VALUES
('SUP001', 'Tech Solutions Inc.', 'Robert Garcia', '(02) 8123-1111', 'robert.garcia@techsolutions.com', 'Makati City, Metro Manila', 'Local', 1, 'Active'),
('SUP002', 'OfficePro Supplies', 'Angela Reyes', '(02) 8456-2222', 'angela.reyes@officepro.com', 'Pasay City, Metro Manila', 'Local', 0, 'Active');

INSERT IGNORE INTO products (product_code, product_name, category_id, supplier_id, quantity, reorder_level, unit_price, status)
SELECT 'P001', 'Keyboard', c.category_id, s.supplier_id, 45, 20, 850.00, 'In Stock'
FROM categories c, suppliers s
WHERE c.category_name = 'Accessories' AND s.supplier_code = 'SUP001';

INSERT IGNORE INTO products (product_code, product_name, category_id, supplier_id, quantity, reorder_level, unit_price, status)
SELECT 'P002', 'Mouse', c.category_id, s.supplier_id, 12, 15, 450.00, 'Low Stock'
FROM categories c, suppliers s
WHERE c.category_name = 'Accessories' AND s.supplier_code = 'SUP001';

INSERT IGNORE INTO products (product_code, product_name, category_id, supplier_id, quantity, reorder_level, unit_price, status)
SELECT 'P003', 'Monitor', c.category_id, s.supplier_id, 20, 10, 6500.00, 'In Stock'
FROM categories c, suppliers s
WHERE c.category_name = 'Electronics' AND s.supplier_code = 'SUP001';

INSERT IGNORE INTO products (product_code, product_name, category_id, supplier_id, quantity, reorder_level, unit_price, status)
SELECT 'P004', 'Printer Ink', c.category_id, s.supplier_id, 5, 10, 750.00, 'Low Stock'
FROM categories c, suppliers s
WHERE c.category_name = 'Office Supplies' AND s.supplier_code = 'SUP002';

INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, reference_no, quantity_before, quantity_change, quantity_after, remarks, created_by)
SELECT p.product_id, 'OPENING', 'PRODUCT', p.product_id, p.product_code, 0, p.quantity, p.quantity, 'Opening stock on seed', NULL
FROM products p
WHERE p.product_code IN ('P001','P002','P003','P004');
