-- ============================================================
-- V4: Bank Reserve Account + Transaction Type
--
-- In real banking, money deposited into a customer account
-- must come FROM somewhere. We create a special "BANK_RESERVE"
-- system account that acts as the source of all deposits.
--
-- This keeps our ledger complete:
--   Every balance change = a transaction record.
--   No money appears or disappears from thin air.
-- ============================================================

-- Step 1: Add a 'transaction_type' column to transactions.
-- This lets us distinguish deposits from transfers.
ALTER TABLE transactions
    ADD COLUMN transaction_type VARCHAR(20) NOT NULL DEFAULT 'TRANSFER';

-- Add constraint for valid types
ALTER TABLE transactions ADD CONSTRAINT chk_txn_type
    CHECK (transaction_type IN ('TRANSFER', 'DEPOSIT', 'WITHDRAWAL'));

-- Step 2: Create a system user that owns the bank reserve account.
-- This user cannot log in (password is a placeholder hash, no real password).
INSERT INTO users (email, password, full_name, role, enabled)
VALUES (
    'system@bank.internal',
    '$2a$10$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
    'Bank System',
    'ADMIN',
    FALSE   -- disabled = cannot log in
);

-- Step 3: Create the BANK_RESERVE account.
-- This account has a very large starting balance to fund all deposits.
-- Its account number is fixed and hardcoded for easy lookup.
INSERT INTO accounts (account_number, owner_id, account_type, balance, currency, status, version)
VALUES (
    'BANK-RESERVE-001',
    (SELECT id FROM users WHERE email = 'system@bank.internal'),
    'CHECKING',
    999999999999.0000,   -- 999 billion — effectively unlimited for our purposes
    'USD',
    'ACTIVE',
    0
);
