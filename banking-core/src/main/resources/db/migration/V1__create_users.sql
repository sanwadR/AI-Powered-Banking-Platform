-- ============================================================
-- V1: Create Users Table
-- This is the first migration Flyway will run.
-- It creates the table that stores everyone who can log in.
-- ============================================================

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,                      -- Auto-incrementing ID
    email       VARCHAR(255) UNIQUE NOT NULL,               -- Must be unique, no duplicates
    password    VARCHAR(255) NOT NULL,                      -- bcrypt hashed password
    full_name   VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'CUSTOMER',   -- CUSTOMER, ADMIN, FRAUD_ANALYST
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,         -- Can this user log in?
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index on email for fast lookups during login
CREATE INDEX idx_users_email ON users(email);

-- Seed an admin user (password = "Admin@123456" - bcrypt hash)
INSERT INTO users (email, password, full_name, role)
VALUES (
    'admin@bank.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Admin',
    'ADMIN'
);
