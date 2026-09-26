-- ============================================================
-- V5: Phase 2 — Payment Reliability
--
-- This migration adds 3 things:
--   1. Update transaction status constraint to include new states
--   2. Idempotency keys table (prevents double payments)
--   3. Transaction events table (full audit trail of state changes)
-- ============================================================

-- ─── 1. Update transaction status constraint ───────────────────
-- Drop old constraint that only allowed PENDING, COMPLETED, FAILED
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS chk_txn_status;

-- Add new constraint that includes Phase 2 states
ALTER TABLE transactions ADD CONSTRAINT chk_txn_status
    CHECK (status IN ('PENDING', 'INITIATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED'));

-- ─── 2. Idempotency Keys table ─────────────────────────────────
-- Stores the result of payment requests.
-- If the same key comes in again, return the stored result instead of re-processing.
--
-- Example:
--   Request 1: Idempotency-Key: abc-123 → processes transfer, stores result
--   Request 2: Idempotency-Key: abc-123 → returns stored result, skips processing
CREATE TABLE idempotency_keys (
    id           BIGSERIAL       PRIMARY KEY,
    idempotency_key VARCHAR(255)  UNIQUE NOT NULL,
    user_id      BIGINT          NOT NULL REFERENCES users(id),
    request_path VARCHAR(500)    NOT NULL,              -- Which API was called
    response     TEXT            NOT NULL,              -- The original response as JSON
    created_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP       NOT NULL DEFAULT NOW() + INTERVAL '24 hours'
);

-- Index for fast lookups by key (used on every payment request)
CREATE INDEX idx_idempotency_key ON idempotency_keys(idempotency_key);

-- ─── 3. Transaction Events table ───────────────────────────────
-- Every time a transaction changes state, a row is inserted here.
-- This gives us a complete timeline:
--   "At 14:00:01 it went from INITIATED to PROCESSING"
--   "At 14:00:02 it went from PROCESSING to COMPLETED"
--
-- Transactions are immutable (we never update them).
-- Transaction events are also immutable (append-only).
CREATE TABLE transaction_events (
    id             BIGSERIAL    PRIMARY KEY,
    transaction_id BIGINT       NOT NULL REFERENCES transactions(id),
    from_status    VARCHAR(20),                   -- NULL for the first event (INITIATED)
    to_status      VARCHAR(20)  NOT NULL,
    reason         TEXT,                          -- Human-readable explanation
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index so we can quickly load all events for one transaction
CREATE INDEX idx_txn_events_transaction_id ON transaction_events(transaction_id);
