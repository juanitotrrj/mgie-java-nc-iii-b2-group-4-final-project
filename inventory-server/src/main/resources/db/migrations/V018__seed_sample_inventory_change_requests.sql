INSERT INTO inventory_change_requests
  (request_no, product_id, request_type, current_quantity, requested_quantity, quantity_change, reason, status, requested_by, requested_at)
SELECT
  'ICR-2026-000001',
  p.product_id,
  'Adjustment',
  p.quantity,
  p.quantity + 5,
  5,
  'Physical count found 5 extra units in storage bin A3.',
  'Pending',
  u.user_id,
  NOW()
FROM products p, users u
WHERE p.product_code = 'P001' AND u.username = 'admin'
LIMIT 1;
