#!/bin/bash
# Quick fix script for production - Add missing VIEW_NOTIFICATION permission
# Run this on production server to fix 403 error for notification endpoints

echo "=========================================="
echo "FIX: Adding VIEW_NOTIFICATION permission"
echo "=========================================="

docker exec -i dentalclinic-postgres psql -U root -d dental_clinic_db <<'EOF'
-- Add VIEW_NOTIFICATION permission if not exists
INSERT INTO permissions (permission_code, permission_name, group_code, description, sort_order, parent_permission_code, is_active, created_at)
VALUES ('VIEW_NOTIFICATION', 'VIEW_NOTIFICATION', 'NOTIFICATION', 'Xem thông báo của bản thân', 300, NULL, TRUE, NOW())
ON CONFLICT (permission_code) DO NOTHING;

-- Add MANAGE_NOTIFICATION permission if not exists
INSERT INTO permissions (permission_code, permission_name, group_code, description, sort_order, parent_permission_code, is_active, created_at)
VALUES ('MANAGE_NOTIFICATION', 'MANAGE_NOTIFICATION', 'NOTIFICATION', 'Quản lý thông báo hệ thống', 301, NULL, TRUE, NOW())
ON CONFLICT (permission_code) DO NOTHING;

-- Grant VIEW_NOTIFICATION to all roles
INSERT INTO role_permissions (role_code, permission_code)
SELECT role_code, 'VIEW_NOTIFICATION'
FROM roles
WHERE role_code IN ('ROLE_DENTIST', 'ROLE_NURSE', 'ROLE_DENTIST_INTERN', 'ROLE_RECEPTIONIST', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT', 'ROLE_INVENTORY_MANAGER', 'ROLE_PATIENT')
ON CONFLICT DO NOTHING;

-- Grant MANAGE_NOTIFICATION to admin roles
INSERT INTO role_permissions (role_code, permission_code)
SELECT role_code, 'MANAGE_NOTIFICATION'
FROM roles
WHERE role_code IN ('ROLE_ADMIN', 'ROLE_MANAGER')
ON CONFLICT DO NOTHING;

-- Show results
SELECT 'Permissions added successfully!' as status;

SELECT 'VIEW_NOTIFICATION permission:' as info;
SELECT COUNT(*) as assigned_roles FROM role_permissions WHERE permission_code = 'VIEW_NOTIFICATION';

SELECT 'MANAGE_NOTIFICATION permission:' as info;
SELECT COUNT(*) as assigned_roles FROM role_permissions WHERE permission_code = 'MANAGE_NOTIFICATION';
EOF

echo "=========================================="
echo "✓ Fix completed!"
echo "=========================================="
echo ""
echo "Verify the fix:"
echo "docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -c \"SELECT p.permission_code, COUNT(rp.role_code) as roles_count FROM permissions p LEFT JOIN role_permissions rp ON p.permission_code = rp.permission_code WHERE p.permission_code LIKE '%NOTIFICATION%' GROUP BY p.permission_code;\""
