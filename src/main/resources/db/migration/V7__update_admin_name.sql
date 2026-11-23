-- V7__update_admin_name.sql
-- Cập nhật tên quản trị viên / người dùng trong bảng app_settings thành Phạm Huỳnh Văn Phát

INSERT INTO app_settings (setting_key, setting_value, updated_at)
VALUES 
    ('ADMIN_FULLNAME', 'Phạm Huỳnh Văn Phát', NOW()),
    ('ADMIN_NAME', 'Phạm Huỳnh Văn Phát', NOW()),
    ('USER_FULLNAME', 'Phạm Huỳnh Văn Phát', NOW())
ON CONFLICT (setting_key) 
DO UPDATE SET setting_value = EXCLUDED.setting_value, updated_at = NOW();
