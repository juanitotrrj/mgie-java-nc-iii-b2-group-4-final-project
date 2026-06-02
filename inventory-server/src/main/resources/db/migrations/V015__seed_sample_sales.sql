INSERT INTO sales (invoice_no, customer_name, cashier_id, sale_date, payment_method, status, subtotal, tax_amount, total_amount, amount_received, change_amount, created_by)
VALUES ('INV-2026-00001', 'Walk-in Customer', 1, '2026-05-20 10:30:00', 'Cash', 'Paid', 1700.00, 0.00, 1700.00, 2000.00, 300.00, 1);

INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, line_total)
SELECT s.sale_id, p.product_id, 2, 850.00, 1700.00
FROM sales s
JOIN products p ON p.product_code = 'P001'
WHERE s.invoice_no = 'INV-2026-00001';
