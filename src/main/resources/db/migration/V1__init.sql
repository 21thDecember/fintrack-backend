-- V1__init.sql
-- Personal Finance Database Schema for PostgreSQL (Neon Compatible)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Accounts Table (Tài khoản)
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL, -- BANK, CASH, OTHER
    bank_code VARCHAR(30),
    account_number VARCHAR(100),
    opening_balance BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Categories Table (Danh mục thu chi)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    icon VARCHAR(50),
    color VARCHAR(20),
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Persons Table (Đối tượng liên quan: bạn bè, người vay mượn)
CREATE TABLE persons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Transactions Table (Giao dịch thu chi, chuyển khoản, vay mượn)
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(id),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL, -- INCOME, EXPENSE, TRANSFER, BORROW_RECEIVED, LOAN_GIVEN, DEBT_REPAYMENT, DEBT_COLLECTION, ADJUSTMENT
    amount BIGINT NOT NULL CHECK (amount > 0),
    direction VARCHAR(10) NOT NULL, -- IN, OUT, NONE
    source VARCHAR(20) NOT NULL, -- BANK, MANUAL, SYSTEM
    external_transaction_id VARCHAR(150),
    external_reference VARCHAR(150),
    original_description TEXT,
    description TEXT,
    note TEXT,
    transaction_date TIMESTAMPTZ NOT NULL,
    classification_status VARCHAR(20) NOT NULL DEFAULT 'UNCLASSIFIED', -- UNCLASSIFIED, CLASSIFIED
    transfer_group_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED', -- POSTED, VOIDED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Unique index for bank/external transactions
CREATE UNIQUE INDEX uq_transactions_source_ext_acc 
ON transactions(source, external_transaction_id, account_id) 
WHERE external_transaction_id IS NOT NULL;

-- 5. Debts Table (Sổ nợ: Phải thu / Phải trả)
CREATE TABLE debts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id UUID NOT NULL REFERENCES persons(id),
    type VARCHAR(20) NOT NULL, -- RECEIVABLE (Phải thu), PAYABLE (Phải trả)
    origin_type VARCHAR(20) NOT NULL, -- TRANSACTION, MANUAL
    origin_transaction_id UUID REFERENCES transactions(id) ON DELETE SET NULL,
    description TEXT,
    principal_amount BIGINT NOT NULL CHECK (principal_amount > 0),
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, PARTIAL, PAID, CANCELLED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Debt Allocations Table (Phân bổ nợ theo giao dịch)
CREATE TABLE debt_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    debt_id UUID NOT NULL REFERENCES debts(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES persons(id),
    allocated_amount BIGINT NOT NULL CHECK (allocated_amount > 0),
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Debt Payments Table (Lịch sử thanh toán/trả nợ)
CREATE TABLE debt_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    debt_id UUID NOT NULL REFERENCES debts(id) ON DELETE CASCADE,
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    amount BIGINT NOT NULL CHECK (amount > 0),
    payment_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. Budgets Table (Ngân sách theo danh mục)
CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    limit_amount BIGINT NOT NULL CHECK (limit_amount >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_budgets_category_year_month UNIQUE (category_id, year, month)
);

-- 9. App Settings Table (Cấu hình ứng dụng)
CREATE TABLE app_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Required Indexes
CREATE INDEX idx_transactions_account_date ON transactions(account_id, transaction_date);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_category_date ON transactions(category_id, transaction_date);
CREATE INDEX idx_transactions_source_external_id ON transactions(source, external_transaction_id);
CREATE INDEX idx_debts_person_status ON debts(person_id, status);
CREATE INDEX idx_debts_type_status ON debts(type, status);
CREATE INDEX idx_debt_payments_debt_date ON debt_payments(debt_id, payment_date);
CREATE INDEX idx_debt_allocations_transaction ON debt_allocations(transaction_id);
CREATE INDEX idx_budgets_category_year_month ON budgets(category_id, year, month);
