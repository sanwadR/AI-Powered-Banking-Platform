-- ============================================================
-- V3: Create Transactions Table
-- Every single money movement is recorded here permanently.
-- Transactions are immutable — we never update or delete them.
-- ============================================================

CREATE TABLE transactions (
    id                  BIGSERIAL       PRIMARY KEY,
    reference_id        UUID            UNIQUE NOT NULL DEFAULT gen_random_uuid(), -- Globally unique ID
    source_account_id   BIGINT          NOT NULL REFERENCES accounts(id),
    target_account_id   BIGINT          NOT NULL REFERENCES accounts(id),
    amount              DECIMAL(19, 4)  NOT NULL,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'USD',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED
    description         TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP       -- Null until the transaction finishes
);

-- Constraint: transaction status must be valid
ALTER TABLE transactions ADD CONSTRAINT chk_txn_status
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'));

-- Constraint: amount must be positive
ALTER TABLE transactions ADD CONSTRAINT chk_txn_amount
    CHECK (amount > 0);

-- Constraint: source and target must be different accounts
ALTER TABLE transactions ADD CONSTRAINT chk_txn_different_accounts
    CHECK (source_account_id <> target_account_id);

-- Indexes for fast queries (e.g., "show me all transactions for account 5")
CREATE INDEX idx_txn_source  ON transactions(source_account_id);
CREATE INDEX idx_txn_target  ON transactions(target_account_id);
CREATE INDEX idx_txn_status  ON transactions(status);
CREATE INDEX idx_txn_created ON transactions(created_at DESC);
