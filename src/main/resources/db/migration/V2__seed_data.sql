-- V2__seed_data.sql
-- Seed default categories for Personal Finance Management

-- Default INCOME categories
INSERT INTO categories (name, type, icon, color, is_active, created_at)
VALUES 
    ('Lương', 'INCOME', 'briefcase', '#10B981', true, NOW()),
    ('Thưởng', 'INCOME', 'gift', '#3B82F6', true, NOW()),
    ('Thu nhập khác', 'INCOME', 'dollar-sign', '#8B5CF6', true, NOW());

-- Default EXPENSE categories
INSERT INTO categories (name, type, icon, color, is_active, created_at)
VALUES 
    ('Ăn uống', 'EXPENSE', 'utensils', '#EF4444', true, NOW()),
    ('Di chuyển', 'EXPENSE', 'car', '#F59E0B', true, NOW()),
    ('Mua sắm', 'EXPENSE', 'shopping-bag', '#EC4899', true, NOW()),
    ('Giải trí', 'EXPENSE', 'film', '#6366F1', true, NOW()),
    ('Sức khỏe', 'EXPENSE', 'heart-pulse', '#14B8A6', true, NOW()),
    ('Giáo dục', 'EXPENSE', 'graduation-cap', '#06B6D4', true, NOW()),
    ('Nhà ở', 'EXPENSE', 'home', '#84CC16', true, NOW()),
    ('Hóa đơn & Tiện ích', 'EXPENSE', 'receipt', '#F97316', true, NOW()),
    ('Khác', 'EXPENSE', 'more-horizontal', '#6B7280', true, NOW());
