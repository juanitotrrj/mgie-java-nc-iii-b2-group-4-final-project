INSERT IGNORE INTO roles (role_name, description, is_system_role) VALUES
('Administrator', 'Full system access, user management, settings, reports, and all inventory modules.', 1),
('Manager', 'Operational management, dashboards, purchases, sales, reports, and approval of inventory change requests.', 1),
('Inventory Clerk', 'Inventory records, suppliers, categories, purchases, and inventory change requests.', 1),
('Cashier', 'Sales processing, product lookup, receipt generation, and cashier dashboard.', 1);

INSERT IGNORE INTO permissions (permission_code, description) VALUES
('AUTH_LOGIN', 'Can login to the system'),
('DASHBOARD_VIEW', 'Can view dashboard'),
('PRODUCT_READ', 'Can view products'),
('PRODUCT_WRITE', 'Can create and update product non-stock fields'),
('PRODUCT_DELETE', 'Can deactivate products'),
('CATEGORY_READ', 'Can view categories'),
('CATEGORY_WRITE', 'Can create and update categories'),
('CATEGORY_DELETE', 'Can deactivate categories'),
('SUPPLIER_READ', 'Can view suppliers'),
('SUPPLIER_WRITE', 'Can create and update suppliers'),
('SUPPLIER_DELETE', 'Can deactivate suppliers'),
('PURCHASE_READ', 'Can view purchase orders'),
('PURCHASE_WRITE', 'Can create and update pending purchase orders'),
('PURCHASE_RECEIVE', 'Can receive purchase orders and increase stock'),
('SALE_READ', 'Can view sales'),
('SALE_WRITE', 'Can create sales and decrease stock'),
('SALE_CANCEL', 'Can cancel sales'),
('REPORT_READ', 'Can generate reports'),
('EXPORT_DATA', 'Can export records and reports'),
('USER_MANAGE', 'Can manage users'),
('SETTINGS_MANAGE', 'Can configure system settings'),
('INVENTORY_CHANGE_REQUEST_CREATE', 'Can submit inventory change requests'),
('INVENTORY_CHANGE_REQUEST_REVIEW', 'Can approve or reject inventory change requests'),
('AUDIT_LOG_READ', 'Can view audit logs'),
('BACKUP_MANAGE', 'Can backup and restore database');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'Administrator';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.permission_code IN (
  'AUTH_LOGIN','DASHBOARD_VIEW','PRODUCT_READ','PRODUCT_WRITE',
  'CATEGORY_READ','CATEGORY_WRITE','SUPPLIER_READ','SUPPLIER_WRITE',
  'PURCHASE_READ','PURCHASE_WRITE','PURCHASE_RECEIVE',
  'SALE_READ','SALE_CANCEL','REPORT_READ','EXPORT_DATA',
  'INVENTORY_CHANGE_REQUEST_REVIEW'
)
WHERE r.role_name = 'Manager';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.permission_code IN (
  'AUTH_LOGIN','DASHBOARD_VIEW','PRODUCT_READ','PRODUCT_WRITE',
  'CATEGORY_READ','CATEGORY_WRITE','SUPPLIER_READ','SUPPLIER_WRITE',
  'PURCHASE_READ','PURCHASE_WRITE','INVENTORY_CHANGE_REQUEST_CREATE'
)
WHERE r.role_name = 'Inventory Clerk';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.permission_code IN (
  'AUTH_LOGIN','DASHBOARD_VIEW','PRODUCT_READ','SALE_READ','SALE_WRITE'
)
WHERE r.role_name = 'Cashier';

-- Default admin user (BCrypt hash of 'Admin@123' with cost 10)
INSERT IGNORE INTO users (user_code, full_name, username, email, password_hash, role_id, status)
SELECT 'U001', 'System Administrator', 'admin', 'admin@inventory.local',
       '$2a$10$0qFPf5cpFOjS5zzKDh3rrOHdHiAnPglD83vcuS.cj7AFR7B4aUzDO',
       role_id, 'Active'
FROM roles WHERE role_name = 'Administrator';

