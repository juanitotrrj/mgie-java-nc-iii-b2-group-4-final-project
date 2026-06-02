INSERT INTO purchase_orders (po_number, supplier_id, order_date, expected_delivery_date, status, total_amount, notes, created_by)
SELECT 'PO-2026-00001', s.supplier_id, '2026-05-15', '2026-05-22', 'Pending', 17000.00, 'Initial stock order', 1
FROM suppliers s WHERE s.supplier_code = 'SUP001';

INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_cost, line_total)
SELECT po.purchase_order_id, p.product_id, 20, 850.00, 17000.00
FROM purchase_orders po
JOIN products p ON p.product_code = 'P001'
WHERE po.po_number = 'PO-2026-00001';
