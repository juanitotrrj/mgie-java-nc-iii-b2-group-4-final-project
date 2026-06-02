INSERT IGNORE INTO permissions (permission_code, description) VALUES
('STOCK_MOVEMENT_READ', 'Can view stock movement audit trail');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.permission_code = 'STOCK_MOVEMENT_READ'
WHERE r.role_name IN ('Administrator', 'Manager', 'Inventory Clerk');
