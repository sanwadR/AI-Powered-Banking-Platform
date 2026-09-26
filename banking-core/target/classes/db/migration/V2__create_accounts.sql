-- ============================================================
-- V2: Create Accounts Table
-- A user can have multiple bank accounts (savings, checking).
-- Each account stores a balance with high precision (DECIMAL 19,4).
-- ============================================================

CREATE TABLE accounts (
    id              BIGSERIAL       PRIMARY KEY,
    account_number  VARCHAR(50)     UNIQUE NOT NULL,    -- e.g. ACC-20241201-000001
    owner_id        BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    account_type    VARCHAR(20)     NOT NULL,           -- SAVINGS or CHECKING
    balance         DECIMAL(19, 4)  NOT NULL DEFAULT 0, -- High precision for money
    currency        VARCHAR(3)      NOT NULL DEFAULT 'USD',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED, FROZEN
    version         BIGINT          NOT NULL DEFAULT 0, -- For optimistic locking (Phase 2)
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Constraint: account type must be one of these values
ALTER TABLE accounts ADD CONSTRAINT chk_account_type
    CHECK (account_type IN ('SAVINGS', 'CHECKING'));

-- Constraint: status must be one of these values
ALTER TABLE accounts ADD CONSTRAINT chk_account_status
    CHECK (status IN ('ACTIVE', 'CLOSED', 'FROZEN'));

-- Constraint: balance cannot go negative
ALTER TABLE accounts ADD CONSTRAINT chk_balance_non_negative
    CHECK (balance >= 0);

-- Index for fast lookups by owner
CREATE INDEX idx_accounts_owner_id ON accounts(owner_id);
CREATE INDEX idx_accounts_number   ON accounts(account_number);
