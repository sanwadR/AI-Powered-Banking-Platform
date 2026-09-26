# 🏦 AI-Driven Banking Risk & Payment Platform
### Complete Architecture Guide — Beginner to Advanced

> **Who is this for?**  
> This document is written for someone who is **completely new** to backend development. Every concept is explained from the ground up. You do not need prior experience. By the end of this project, you will have built a production-grade banking backend — the same type of system used by real banks.

---

## 📖 Table of Contents

1. [What Are We Building?](#1-what-are-we-building)
2. [How to Read This Document](#2-how-to-read-this-document)
3. [Core Concepts You Need to Know First](#3-core-concepts-you-need-to-know-first)
4. [Technology Stack — What & Why](#4-technology-stack--what--why)
5. [Project Phases Overview](#5-project-phases-overview)
6. [Phase 1 — Core Banking Foundation](#phase-1--core-banking-foundation)
7. [Phase 2 — Payment Processing & Reliability](#phase-2--payment-processing--reliability)
8. [Phase 3 — Fraud Detection (Rule-Based)](#phase-3--fraud-detection-rule-based)
9. [Phase 4 — Event-Driven Architecture with Kafka](#phase-4--event-driven-architecture-with-kafka)
10. [Phase 5 — Caching & Performance with Redis](#phase-5--caching--performance-with-redis)
11. [Phase 6 — ML-Powered Fraud Detection](#phase-6--ml-powered-fraud-detection)
12. [Phase 7 — GenAI Fraud Investigation Assistant](#phase-7--genai-fraud-investigation-assistant)
13. [Full System Architecture Diagram](#13-full-system-architecture-diagram)
14. [Database Design — Tables & Relationships](#14-database-design--tables--relationships)
15. [API Design & Endpoints](#15-api-design--endpoints)
16. [Security Architecture](#16-security-architecture)
17. [Folder Structure](#17-folder-structure)
18. [Development Environment Setup](#18-development-environment-setup)
19. [Learning Roadmap](#19-learning-roadmap)
20. [Glossary](#20-glossary)

---

## 1. What Are We Building?

Imagine you are building the **backend** (the server-side brain) of a digital bank, similar to what powers apps like **Revolut, N26, or Monzo**.

### What users will be able to do:
- 🔐 Register and log in securely
- 🏦 Open and manage bank accounts
- 💸 Transfer money between accounts
- 📊 View transaction history
- 🚨 Get alerted when suspicious activity is detected
- 🤖 Have an AI assistant explain why a transaction was flagged as fraud

### What the system will do internally:
- Validate and process payments reliably (no double charges)
- Detect fraudulent transactions using rules, then machine learning
- Publish events when things happen (e.g., "payment made") so other services can react
- Cache frequently-accessed data to serve responses in milliseconds
- Investigate fraud using a conversational AI model

---

## 2. How to Read This Document

This document is structured so you can **read it top to bottom** and understand everything step by step.

| Symbol | Meaning |
|--------|---------|
| 🟢 | Beginner-friendly concept, explained from scratch |
| 🟡 | Intermediate — builds on earlier concepts |
| 🔴 | Advanced — introduced in later phases |
| 💡 | Important tip or insight |
| ⚠️ | Common mistake to avoid |
| 📦 | A specific technology being introduced |

---

## 3. Core Concepts You Need to Know First

Before writing a single line of code, you must understand these foundational ideas. **Don't skip this section.**

---

### 3.1 🟢 What is a Backend?

When you open a banking app, you see a screen (that is the **frontend**). But where is your account balance stored? Where is the logic that says "you can't transfer more money than you have"?

That lives in the **backend** — a program running on a server somewhere that:
- Stores data in a database
- Applies business rules (e.g., no overdrafts)
- Responds to requests from the app

```
You (App) ──── sends request ────► Backend Server ──── reads/writes ────► Database
           ◄─── sends response ───
```

---

### 3.2 🟢 What is an API?

An **API (Application Programming Interface)** is how the frontend talks to the backend. Think of it like a restaurant menu:
- You (the frontend) look at the menu and place an order
- The kitchen (the backend) prepares it and sends it back
- You don't need to know how the kitchen works

In our system, we'll build a **REST API** — a standard way of designing these menus using HTTP (the language of the web).

```
GET    /accounts/123        → "Give me account 123's details"
POST   /transfers           → "Make a new money transfer"
DELETE /accounts/123        → "Close account 123"
```

---

### 3.3 🟢 What is a Database?

A database is where data lives permanently. Even if the server crashes and restarts, the data stays safe.

We'll use **PostgreSQL** — a powerful, open-source relational database used by companies like Instagram, Uber, and Spotify.

In a relational database, data is stored in **tables** (like spreadsheets):

```
accounts table:
| id  | owner_name | balance  | status |
|-----|------------|----------|--------|
| 1   | Alice      | 5000.00  | ACTIVE |
| 2   | Bob        | 2500.00  | ACTIVE |
```

Tables are linked to each other through **foreign keys** — like how a `transactions` table can reference which account made the transfer.

---

### 3.4 🟢 What is a Framework?

Writing a server from scratch would take months. A **framework** is pre-written code that handles the boring stuff (like receiving HTTP requests) so you can focus on business logic.

We'll use **Spring Boot** (Java):
- Industry standard for enterprise banking applications
- Used by banks, fintechs, and large companies worldwide
- Handles HTTP, database connections, security, and much more

---

### 3.5 🟡 What is Event-Driven Architecture?

Imagine a bank where every time a payment is made, 5 different things need to happen:
1. Send an SMS notification
2. Check for fraud
3. Update the customer's monthly statement
4. Log the event for auditing
5. Update analytics dashboards

**Option A (Bad):** The payment service calls all 5 services one by one.
→ Problem: Slow, tightly coupled, one failure breaks everything.

**Option B (Good — Event-Driven):** The payment service just says "Hey, a payment happened!" and each other service listens and reacts independently.

This "announcement" is called an **event**, and the system that routes events is called a **message broker** (we'll use **Apache Kafka**).

```
Payment Service ──publishes──► Kafka Topic: "payment.completed"
                                    │
                    ┌───────────────┼────────────────┐
                    ▼               ▼                 ▼
              Notification    Fraud Detection    Analytics
               Service           Service          Service
```

---

### 3.6 🟡 What is Caching?

Every time a user views their account balance, the server queries the database. Database queries are slow (10–100ms each).

A **cache** is a super-fast in-memory storage that keeps copies of frequently-requested data:
- First request: Query database (slow), store result in cache
- Next 1000 requests: Read from cache (microseconds, not milliseconds)

We'll use **Redis** as our cache.

---

### 3.7 🔴 What is Machine Learning in This Context?

We'll train a model that looks at thousands of past transactions and learns what "normal" looks like. When a new transaction comes in, the model scores it: 0.0 = definitely legitimate, 1.0 = definitely fraudulent.

This is called **anomaly detection** — finding things that don't fit the normal pattern.

---

### 3.8 🔴 What is GenAI?

**Generative AI** (like GPT-4 or Gemini) can read text and generate human-readable explanations. In our system, a fraud analyst can ask:

> "Why was transaction #8821 flagged as suspicious?"

And the AI assistant will read the transaction data, fraud score, and rules that triggered, then write a clear explanation — like having an expert analyst on call 24/7.

---

## 4. Technology Stack — What & Why

| Layer | Technology | Why We Chose It |
|-------|-----------|-----------------|
| **Language** | Java 21 (with Spring Boot 3) | Industry standard for banking; statically typed, mature, safe |
| **Framework** | Spring Boot 3 | Most popular Java framework; built-in security, REST, DB support |
| **Database** | PostgreSQL 16 | ACID-compliant, trusted by banks, powerful querying |
| **ORM** | Spring Data JPA / Hibernate | Maps Java objects to database tables automatically |
| **Migration** | Flyway | Tracks database schema changes like Git tracks code |
| **Security** | Spring Security + JWT | Industry-standard authentication and authorization |
| **Message Broker** | Apache Kafka | High-throughput, fault-tolerant event streaming |
| **Cache** | Redis | Sub-millisecond in-memory data store |
| **Build Tool** | Maven | Manages dependencies and builds the project |
| **API Docs** | Swagger / OpenAPI | Auto-generates interactive API documentation |
| **Containerization** | Docker + Docker Compose | Run everything (DB, Kafka, Redis) with one command |
| **Testing** | JUnit 5 + Mockito + Testcontainers | Unit, integration, and end-to-end testing |
| **ML** | Python + scikit-learn (sidecar service) | Fraud scoring model served via REST |
| **GenAI** | OpenAI / Gemini API | Conversational fraud investigation assistant |
| **Monitoring** | Spring Actuator + Micrometer | Health checks and metrics |

> 💡 **Why Java, not Python or Node.js?**
> Real banks use Java. It's strongly typed (fewer bugs), has a massive ecosystem for banking (Spring ecosystem, enterprise libraries), and its performance characteristics are predictable under load.

---

## 5. Project Phases Overview

Each phase **builds on the previous one**. Every phase ends with a **fully working system**. You never break what you've already built.

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7
  Core      Payment      Fraud       Kafka        Redis         ML          AI
 Banking   Processing   (Rules)     Events      Caching      Scoring   Investigator
```

| Phase | What You'll Build | New Technologies |
|-------|-------------------|-----------------|
| **1** | User auth, accounts, basic transfers | Spring Boot, PostgreSQL, JWT |
| **2** | Idempotent payments, transaction states, retries | Spring Retry, database locking |
| **3** | Rule-based fraud detection, risk scoring | Business rule engine |
| **4** | Event-driven notifications and fraud pipeline | Apache Kafka |
| **5** | Caching, rate limiting, session management | Redis |
| **6** | ML fraud scoring integrated into payment flow | Python ML sidecar, REST |
| **7** | Conversational AI fraud investigation | OpenAI/Gemini API, Spring AI |

---

## Phase 1 — Core Banking Foundation

### 🎯 Goal
By the end of Phase 1, you will have a running REST API that allows users to register, log in, open bank accounts, and transfer money. This is the foundation everything else is built on.

### What to Build

#### 1.1 User Authentication
- Register a new customer (name, email, password)
- Log in and receive a **JWT token** (a secure, signed string proving who you are)
- All subsequent requests include this token in the Authorization header

**What is JWT?**
JWT (JSON Web Token) is like a signed hall pass. When you log in, the server creates a token that says "This is Alice, she logged in at 2:00 PM." The server signs it cryptographically. Every future request includes this token. The server verifies the signature without having to look up the database every time.

```
Login ──► Server creates JWT: "eyJhbGci..." ──► Client stores it
Future Request: "Authorization: Bearer eyJhbGci..."
Server verifies signature ──► Knows it's Alice ──► Allows access
```

#### 1.2 Account Management
- Create a bank account (SAVINGS or CHECKING)
- Each account gets a unique account number (e.g., ACC-20241201-000001)
- View account details and balance
- Close an account (only if balance is zero)

#### 1.3 Money Transfers
- Transfer money between two accounts
- Validate: source account exists, belongs to logged-in user, has enough balance
- Both accounts update atomically (all-or-nothing — no money lost in transit)

**What is an Atomic Transaction?**
If Alice transfers $100 to Bob:
- Step 1: Deduct $100 from Alice's account
- Step 2: Add $100 to Bob's account

What if the server crashes between Step 1 and Step 2? Alice loses $100 and Bob gets nothing! A **database transaction** ensures both steps happen together or neither does.

### Phase 1 File Structure

```
src/
├── main/
│   ├── java/com/bank/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        # JWT security setup
│   │   │   └── ApplicationConfig.java     # General config
│   │   ├── controller/
│   │   │   ├── AuthController.java        # /auth/register, /auth/login
│   │   │   ├── AccountController.java     # /accounts
│   │   │   └── TransferController.java    # /transfers
│   │   ├── service/
│   │   │   ├── AuthService.java           # Business logic for auth
│   │   │   ├── AccountService.java        # Business logic for accounts
│   │   │   └── TransferService.java       # Business logic for transfers
│   │   ├── repository/
│   │   │   ├── UserRepository.java        # Database queries for users
│   │   │   ├── AccountRepository.java     # Database queries for accounts
│   │   │   └── TransactionRepository.java # Database queries for transactions
│   │   ├── model/
│   │   │   ├── User.java                  # User entity
│   │   │   ├── Account.java               # Account entity
│   │   │   └── Transaction.java           # Transaction entity
│   │   ├── dto/
│   │   │   ├── RegisterRequest.java       # Request body for registration
│   │   │   ├── LoginRequest.java          # Request body for login
│   │   │   └── TransferRequest.java       # Request body for transfer
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       └── InsufficientFundsException.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__create_users.sql
│           ├── V2__create_accounts.sql
│           └── V3__create_transactions.sql
```

> 💡 **Why separate Controller, Service, and Repository?**
> This is called **Layered Architecture**. Each layer has one job:
> - **Controller**: Receive HTTP request, validate input, call Service, return response
> - **Service**: Contains all business logic (rules, calculations)
> - **Repository**: Only talks to the database
>
> This makes the code testable, maintainable, and easy to understand.

### Phase 1 Database Tables

```sql
-- Users who can log in to the system
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,       -- bcrypt hashed, NEVER plaintext
    full_name   VARCHAR(255) NOT NULL,
    role        VARCHAR(50) DEFAULT 'CUSTOMER',
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Bank accounts owned by users
CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    account_number  VARCHAR(50) UNIQUE NOT NULL,
    owner_id        BIGINT REFERENCES users(id),
    account_type    VARCHAR(20) NOT NULL,    -- SAVINGS or CHECKING
    balance         DECIMAL(19,4) NOT NULL DEFAULT 0,
    currency        VARCHAR(3) DEFAULT 'USD',
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT NOW()
);

-- Every money movement is recorded here
CREATE TABLE transactions (
    id                  BIGSERIAL PRIMARY KEY,
    reference_id        UUID UNIQUE NOT NULL,
    source_account_id   BIGINT REFERENCES accounts(id),
    target_account_id   BIGINT REFERENCES accounts(id),
    amount              DECIMAL(19,4) NOT NULL,
    currency            VARCHAR(3) DEFAULT 'USD',
    status              VARCHAR(20) DEFAULT 'PENDING',
    description         TEXT,
    created_at          TIMESTAMP DEFAULT NOW(),
    completed_at        TIMESTAMP
);
```

---

## Phase 2 — Payment Processing & Reliability

### 🎯 Goal
Make payments **production-reliable**. In the real world, networks fail, servers crash, users accidentally click "Pay" twice. Phase 2 makes the system handle all of that gracefully.

### What to Build

#### 2.1 Idempotency
**Problem:** User clicks "Transfer" twice because the first response was slow. Should the bank charge them twice?

**Solution — Idempotency Keys:**
The client sends a unique `Idempotency-Key` header with every payment request. The server stores this key. If the same key comes in again, return the original result instead of processing again.

```
First request:  POST /transfers  Idempotency-Key: uuid-abc123  ──► Processed, saved
Second request: POST /transfers  Idempotency-Key: uuid-abc123  ──► Returns saved result
```

#### 2.2 Transaction State Machine
A transaction moves through defined states:

```
INITIATED ──► PROCESSING ──► COMPLETED
                   │
                   └──► FAILED ──► REFUNDED (if applicable)
```

#### 2.3 Optimistic Locking
**Problem:** Two requests try to deduct from the same account at the same time.

**Solution:** Each account row has a `version` number. When you read it (version=5) and try to save, PostgreSQL checks if it's still version=5. If someone else changed it (version=6), your update fails and retries. This is called **Optimistic Locking**.

#### 2.4 Scheduled Retry for Stuck Transactions
Any transaction stuck in PROCESSING for more than 5 minutes is automatically retried or marked FAILED.

### New Database Elements in Phase 2

```sql
CREATE TABLE idempotency_keys (
    key         VARCHAR(255) PRIMARY KEY,
    response    JSONB NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    expires_at  TIMESTAMP NOT NULL
);

CREATE TABLE transaction_events (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT REFERENCES transactions(id),
    from_status     VARCHAR(20),
    to_status       VARCHAR(20) NOT NULL,
    reason          TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

ALTER TABLE accounts ADD COLUMN version BIGINT DEFAULT 0;
```

---

## Phase 3 — Fraud Detection (Rule-Based)

### 🎯 Goal
Add a rule-based fraud detection engine that evaluates every transaction and assigns a **risk score**. Suspicious transactions are flagged and optionally blocked.

### What to Build

#### 3.1 Fraud Rules Engine

| Rule | Description | Score |
|------|-------------|-------|
| HIGH_AMOUNT | Transaction > $10,000 | +30 |
| UNUSUAL_HOUR | Transaction between 1am–5am | +20 |
| NEW_ACCOUNT | Account created < 30 days ago | +15 |
| RAPID_SUCCESSION | 3+ transactions in 5 minutes | +25 |
| INTERNATIONAL | Cross-currency transfer | +20 |
| DORMANT_REACTIVATION | No activity in 90+ days | +20 |
| ROUND_AMOUNT | Exact round amounts (e.g., $500.00) | +10 |

**Risk Level Thresholds:**
- 0–30: LOW — Auto-approve
- 31–60: MEDIUM — Approve with alert
- 61–80: HIGH — Flag for review
- 81–100: CRITICAL — Auto-block

#### 3.2 Fraud Cases
When a transaction is flagged HIGH or CRITICAL, create a Fraud Case for human review.

#### 3.3 Alert System
- Send in-app alerts for MEDIUM+ risk
- Automatically block CRITICAL transactions
- Allow admins to approve or decline flagged transactions

### New Database Elements in Phase 3

```sql
CREATE TABLE fraud_evaluations (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT REFERENCES transactions(id),
    risk_score      INTEGER NOT NULL,
    risk_level      VARCHAR(20) NOT NULL,
    rules_triggered JSONB,
    evaluated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE fraud_cases (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT REFERENCES transactions(id),
    status          VARCHAR(20) DEFAULT 'OPEN',
    assigned_to     BIGINT REFERENCES users(id),
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    resolved_at     TIMESTAMP
);

CREATE TABLE fraud_rules (
    id          BIGSERIAL PRIMARY KEY,
    rule_name   VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    score       INTEGER NOT NULL,
    enabled     BOOLEAN DEFAULT TRUE,
    parameters  JSONB
);
```

### Phase 3 Payment Flow

```
Transfer Request
      │
      ▼
Validate & Lock Accounts
      │
      ▼
Run Fraud Engine ──► Calculate Risk Score
      │                       │
      │              ┌────────┴────────┐
      │          LOW/MEDIUM         HIGH/CRITICAL
      │              │                  │
      │         Proceed            Block & Create
      │                             Fraud Case
      ▼
Update Balances (if approved)
      │
      ▼
Record Transaction + Fraud Evaluation
```

---

## Phase 4 — Event-Driven Architecture with Kafka

### 🎯 Goal
Decouple services using **Apache Kafka** so that payment processing, fraud detection, and notifications all operate independently.

### What is Apache Kafka?

Kafka is a **distributed message broker**. Think of it as a post office for events:
- **Producers** drop off messages (events)
- **Topics** are mailboxes sorted by type
- **Consumers** pick up and process messages

Messages are stored durably — even if the consumer is down, messages wait until it comes back up.

### Kafka Topics

| Topic Name | Produced By | Consumed By | Purpose |
|-----------|-------------|-------------|---------|
| payment.initiated | Payment Service | Fraud Service | Trigger fraud check |
| payment.completed | Payment Service | Notification, Analytics | Payment success |
| payment.failed | Payment Service | Notification Service | Payment failure |
| fraud.detected | Fraud Service | Notification, Case Manager | Fraud alert |
| fraud.case.created | Case Manager | Notification, AI Investigator | Case opened |

### Phase 4 Architecture

```
                    ┌─────────────────────────────────────┐
                    │             Apache Kafka              │
                    │                                       │
Payment  ──────────►│  payment.initiated                   │──────────► Fraud
Service             │  payment.completed                   │           Service
                    │  payment.failed                      │
                    │                                       │──────────► Notification
Fraud    ──────────►│  fraud.detected                      │           Service
Service             │  fraud.case.created                  │
                    │                                       │──────────► Analytics
                    └─────────────────────────────────────┘           Service
```

### Dead Letter Queue (DLQ)
If processing a message fails 3 times, it goes to a **Dead Letter Topic** (`payment.failed.dlq`). This prevents one bad message from blocking all others.

---

## Phase 5 — Caching & Performance with Redis

### 🎯 Goal
Use **Redis** to speed up the system and add rate limiting to protect against abuse.

### What is Redis?

Redis (Remote Dictionary Server) is an **in-memory key-value store**. It stores data in RAM instead of disk, making it 100–1000x faster than a traditional database for reads.

### What to Build

#### 5.1 Account Balance Caching
```
GET /accounts/123/balance
    │
    ▼
Check Redis cache ──► Cache hit? ──► Return cached value
    │
    ▼ Cache miss
Query PostgreSQL ──► Store in Redis (TTL: 30 seconds) ──► Return value
```

When a transfer completes, **invalidate** (delete) the cached balances for both accounts.

#### 5.2 Rate Limiting
Prevent abuse: allow a maximum of **10 transfers per minute** per user.

```
Key:   rate_limit:user:42:transfers:2024-12-01T14:00
Value: 7                   (current count)
TTL:   60 seconds          (auto-expires after 1 minute)
```

If the count reaches 10, return HTTP 429 (Too Many Requests).

#### 5.3 Token Blacklisting
When a user logs out, their JWT token is added to a Redis blacklist so it's immediately invalidated.

#### 5.4 Distributed Lock
When two requests try to process the same account simultaneously, Redis acts as a **distributed lock** that automatically expires if the holder crashes.

---

## Phase 6 — ML-Powered Fraud Detection

### 🎯 Goal
Replace (or supplement) the rule-based fraud detector with a **machine learning model** that learns patterns from historical data.

### Architecture: ML Sidecar Service

The ML model is written in **Python** and exposed as a small REST API. The Java backend calls it during payment processing.

```
Java Banking Backend
        │
        │  POST /score  {"amount": 5000, "hour": 2, ...}
        ▼
Python ML Service (Flask/FastAPI)
        │
        ▼
scikit-learn Model ──► Returns {"fraud_probability": 0.92}
        │
        ▼
Java Backend uses score to make decision
```

### Feature Engineering

| Feature | Description | Example |
|---------|-------------|---------|
| amount_zscore | How unusual is this amount for this user? | 3.2 (very unusual) |
| hour_of_day | 0–23, what hour? | 3 (3 AM) |
| is_weekend | Boolean | 1 |
| account_age_days | How old is the source account? | 7 |
| txn_count_1h | Transactions in last 1 hour | 8 |
| txn_count_24h | Transactions in last 24 hours | 15 |
| amount_vs_avg | Amount vs. user's average | 12.5 (12.5x average) |
| recipient_seen_before | Has user sent to this account before? | 0 (never) |

### Model Training Pipeline

```
Historical Transactions ──► Feature Extraction ──► Model Training
                                                          │
                                              RandomForestClassifier
                                                          │
                                                   Model Saved (.pkl)
                                                          │
                                                   Flask API loads it
```

### Combined Scoring

Final score = weighted combination of both engines:
```
final_score = 0.4 × rule_score + 0.6 × ml_score
```

---

## Phase 7 — GenAI Fraud Investigation Assistant

### 🎯 Goal
Add an AI assistant that helps fraud analysts investigate flagged transactions using natural language.

### What to Build

#### 7.1 Conversational Interface
A fraud analyst can ask questions like:
- "Why was transaction #8821 flagged?"
- "Show me all suspicious transactions for user Alice in the last 7 days"
- "What's the risk profile of account ACC-000123?"

#### 7.2 Spring AI Integration

```
Analyst Query
      │
      ▼
Spring AI Controller
      │
      ▼
Context Builder ──► Fetch transaction data, fraud scores, rules triggered
      │
      ▼
Prompt Template ──► "You are a fraud analyst. Here is the data: [data].
                     The analyst asks: [question]. Respond professionally."
      │
      ▼
OpenAI / Gemini API ──► Returns explanation
      │
      ▼
Return to analyst
```

#### 7.3 Tool Calling (Function Calling)
The AI can call pre-defined functions to fetch live data:
- `getTransaction(id)` — fetch transaction details
- `getAccountHistory(accountId, days)` — recent transactions
- `getFraudEvaluation(transactionId)` — fraud scores and rules
- `getSimilarCases(pattern)` — find similar past cases

#### 7.4 Conversation History
Each analyst session maintains history so the AI remembers context:
```
Analyst: "Why was transaction #8821 flagged?"
AI:      "Transaction #8821 was flagged for 3 reasons: ..."
Analyst: "What about the account that sent it?"    ← AI knows "it" = #8821
AI:      "Account ACC-000043 has been active for..."
```

---

## 13. Full System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Client Applications                                   │
│                    (Web App / Mobile / Postman)                              │
└────────────────────────────┬────────────────────────────────────────────────┘
                             │ HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Spring Boot Banking API                                 │
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │    Auth      │  │   Accounts   │  │   Payments   │  │     Fraud     │  │
│  │  Controller  │  │  Controller  │  │  Controller  │  │   Controller  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬────────┘  │
│         │                 │                  │                  │           │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼────────┐  │
│  │    Auth      │  │   Account    │  │   Payment    │  │    Fraud      │  │
│  │   Service    │  │   Service    │  │   Service    │  │   Service     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬────────┘  │
└─────────┼─────────────────┼──────────────────┼──────────────────┼───────────┘
          │                 │                  │                  │
          ▼                 ▼                  ▼                  │
┌─────────────────────────────────────────────┐                  │
│              PostgreSQL Database             │                  │
│  users │ accounts │ transactions │ fraud_*   │                  │
└─────────────────────────────────────────────┘                  │
                                                                  │
          ┌───────────────────────────────────────────────────────┘
          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Apache Kafka                                         │
│    payment.initiated │ payment.completed │ fraud.detected │ ...              │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                     ▼
┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Notification   │  │    Analytics     │  │  AI Investigator  │
│    Service      │  │    Service       │  │     Service       │
└─────────────────┘  └──────────────────┘  └────────┬─────────┘
                                                     │
                                           ┌─────────▼──────────┐
                                           │  OpenAI / Gemini   │
                                           │      API           │
                                           └────────────────────┘

Redis (Shared Cache & Rate Limiter) ◄──► All Services

Python ML Sidecar ◄──► Fraud Service
```

---

## 14. Database Design — Tables & Relationships

### Entity Relationship Overview

```
users ──────────── accounts (one user → many accounts)
                      │
                      ├── transactions (source_account / target_account)
                      │        │
                      │        ├── fraud_evaluations
                      │        │
                      │        └── transaction_events (audit trail)
                      │
                      └── fraud_cases
```

### Complete Table Reference

| Table | Purpose | Phase |
|-------|---------|-------|
| users | Customer and admin accounts | 1 |
| accounts | Bank accounts | 1 |
| transactions | All money movements | 1 |
| idempotency_keys | Prevent duplicate payments | 2 |
| transaction_events | Audit trail of state changes | 2 |
| fraud_rules | Configurable rule definitions | 3 |
| fraud_evaluations | Fraud check result per transaction | 3 |
| fraud_cases | High-risk cases for human review | 3 |
| ml_predictions | ML model scores per transaction | 6 |
| ai_conversations | GenAI session history | 7 |

---

## 15. API Design & Endpoints

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/auth/register | Register new customer | None |
| POST | /api/v1/auth/login | Log in, get JWT token | None |
| POST | /api/v1/auth/logout | Invalidate token | JWT |
| POST | /api/v1/auth/refresh | Refresh expired token | JWT |

### Accounts
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/accounts | Open new account | JWT |
| GET | /api/v1/accounts | List my accounts | JWT |
| GET | /api/v1/accounts/{id} | Get account details | JWT |
| DELETE | /api/v1/accounts/{id} | Close account | JWT |
| GET | /api/v1/accounts/{id}/transactions | Get transaction history | JWT |

### Transfers
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/transfers | Initiate transfer | JWT |
| GET | /api/v1/transfers/{id} | Get transfer status | JWT |
| GET | /api/v1/transfers/{id}/events | Get audit trail | JWT |

### Fraud (Admin)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/admin/fraud/cases | List all fraud cases | JWT + ADMIN |
| GET | /api/v1/admin/fraud/cases/{id} | Get case details | JWT + ADMIN |
| PUT | /api/v1/admin/fraud/cases/{id}/resolve | Resolve a case | JWT + ADMIN |
| GET | /api/v1/admin/fraud/rules | List fraud rules | JWT + ADMIN |
| PUT | /api/v1/admin/fraud/rules/{id} | Update a rule | JWT + ADMIN |
| POST | /api/v1/admin/fraud/investigate | Ask AI investigator | JWT + ADMIN |

### Health & Monitoring
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /actuator/health | System health check | None |
| GET | /actuator/metrics | Application metrics | JWT + ADMIN |
| GET | /swagger-ui.html | API documentation | None |

---

## 16. Security Architecture

### 16.1 Authentication Flow
```
1. Client sends email + password to POST /auth/login
2. Server verifies password hash (bcrypt)
3. Server generates JWT:
   - Header: {"alg": "HS256", "typ": "JWT"}
   - Payload: {"sub": "42", "role": "CUSTOMER", "exp": 1700000000}
   - Signature: HMAC-SHA256(header.payload, secret_key)
4. Client stores JWT (memory or secure cookie)
5. Every request: "Authorization: Bearer <token>"
6. Server validates signature on every request
```

### 16.2 Authorization (Role-Based Access Control)
| Role | What They Can Do |
|------|-----------------|
| CUSTOMER | Manage own accounts, make transfers |
| SUPPORT | View all accounts (read-only) |
| FRAUD_ANALYST | View fraud cases, use AI investigator |
| ADMIN | Everything, including managing users and rules |

### 16.3 Security Best Practices Applied
- Passwords hashed with **bcrypt** (never stored plaintext)
- JWT signed with **HS256** (secret) or **RS256** (keypair)
- HTTPS enforced (TLS)
- SQL injection prevented by **parameterized queries** (JPA handles this)
- Sensitive data (account numbers) masked in logs
- Rate limiting on auth endpoints (prevent brute force)
- CORS configured to only allow your frontend origin

---

## 17. Folder Structure

```
AI_Powered_Payment_and_Fraud_Detection/
├── ARCHITECTURE.md                     ← This file
├── README.md                           ← Quick start guide
├── docker-compose.yml                  ← Runs Postgres, Kafka, Redis
├── docker-compose.dev.yml              ← Development overrides
│
├── banking-core/                       ← Main Spring Boot application
│   ├── pom.xml                         ← Maven dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/bank/
│       │   │   ├── BankingApplication.java
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── KafkaConfig.java
│       │   │   │   ├── RedisConfig.java
│       │   │   │   └── SwaggerConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── AccountController.java
│       │   │   │   ├── TransferController.java
│       │   │   │   ├── FraudController.java
│       │   │   │   └── AdminController.java
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── AccountService.java
│       │   │   │   ├── TransferService.java
│       │   │   │   ├── FraudRuleEngine.java
│       │   │   │   ├── FraudCaseService.java
│       │   │   │   ├── MLScoringService.java
│       │   │   │   ├── AIInvestigatorService.java
│       │   │   │   └── NotificationService.java
│       │   │   ├── kafka/
│       │   │   │   ├── producer/
│       │   │   │   │   └── PaymentEventProducer.java
│       │   │   │   └── consumer/
│       │   │   │       ├── FraudDetectionConsumer.java
│       │   │   │       └── NotificationConsumer.java
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── AccountRepository.java
│       │   │   │   ├── TransactionRepository.java
│       │   │   │   ├── FraudEvaluationRepository.java
│       │   │   │   └── FraudCaseRepository.java
│       │   │   ├── model/
│       │   │   │   ├── User.java
│       │   │   │   ├── Account.java
│       │   │   │   ├── Transaction.java
│       │   │   │   ├── FraudEvaluation.java
│       │   │   │   └── FraudCase.java
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   └── response/
│       │   │   ├── event/
│       │   │   │   ├── PaymentInitiatedEvent.java
│       │   │   │   ├── PaymentCompletedEvent.java
│       │   │   │   └── FraudDetectedEvent.java
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── InsufficientFundsException.java
│       │   │       ├── AccountNotFoundException.java
│       │   │       └── DuplicateTransactionException.java
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/
│       │           ├── V1__create_users.sql
│       │           ├── V2__create_accounts.sql
│       │           ├── V3__create_transactions.sql
│       │           ├── V4__create_idempotency.sql
│       │           ├── V5__create_fraud_tables.sql
│       │           └── V6__create_ml_ai_tables.sql
│       └── test/
│           └── java/com/bank/
│               ├── unit/
│               └── integration/
│
├── ml-fraud-service/                   ← Python ML sidecar
│   ├── requirements.txt
│   ├── app.py                          ← Flask/FastAPI server
│   ├── model/
│   │   ├── train.py                    ← Model training script
│   │   ├── features.py                 ← Feature engineering
│   │   └── fraud_model.pkl             ← Saved trained model
│   └── Dockerfile
│
└── docs/
    ├── PHASE_1_GUIDE.md
    ├── PHASE_2_GUIDE.md
    ├── API_REFERENCE.md
    └── postman/
        └── BankingAPI.postman_collection.json
```

---

## 18. Development Environment Setup

### Prerequisites (Install These First)

| Tool | Purpose | Install From |
|------|---------|-------------|
| Java 21 | Run the application | adoptium.net |
| Maven 3.9+ | Build tool | maven.apache.org |
| Docker Desktop | Run Postgres, Kafka, Redis | docker.com |
| IntelliJ IDEA Community | IDE | jetbrains.com |
| Postman | Test APIs | postman.com |
| Git | Version control | git-scm.com |
| Python 3.10+ | ML service (Phase 6+) | python.org |

### Starting the Infrastructure (One Command)

```bash
# Start PostgreSQL, Kafka, Redis, and Zookeeper
docker-compose up -d

# Check everything is running
docker-compose ps
```

### Starting the Application

```bash
# Phase 1: No Kafka/Redis needed
cd banking-core
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Your First API Call

```bash
# Register a user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"SecurePass123!","fullName":"Alice Johnson"}'

# Log in and get token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"SecurePass123!"}'
# Returns: {"token": "eyJhbGci..."}
```

---

## 19. Learning Roadmap

### Phase 1 Prerequisites (2–3 weeks)
- [ ] **Java Basics**: Variables, classes, methods, interfaces
  - Resource: "Java Programming for Beginners" — freeCodeCamp YouTube
- [ ] **Spring Boot Introduction**: What is a bean? What is dependency injection?
  - Resource: Spring Boot Tutorial — Amigoscode YouTube
- [ ] **REST APIs**: HTTP verbs, status codes, JSON
  - Resource: REST API Crash Course — Traversy Media YouTube
- [ ] **SQL Basics**: SELECT, INSERT, UPDATE, JOIN
  - Resource: SQLZoo (free, interactive)
- [ ] **Git Basics**: Commit, push, branch
  - Resource: Git official docs

### Phase 2–3 (2–3 weeks)
- [ ] Spring Security + JWT
- [ ] JPA/Hibernate (ORM)
- [ ] Flyway migrations
- [ ] Exception handling in Spring

### Phase 4 (1–2 weeks)
- [ ] What is Apache Kafka? (read Confluent's free intro)
- [ ] Spring Kafka
- [ ] Consumer groups and partitioning

### Phase 5 (1 week)
- [ ] Redis data structures
- [ ] Spring Data Redis
- [ ] Cache-aside pattern

### Phase 6 (2–3 weeks)
- [ ] Python basics (if needed)
- [ ] pandas, scikit-learn
- [ ] Building a REST API with FastAPI
- [ ] Docker

### Phase 7 (1–2 weeks)
- [ ] Prompt engineering
- [ ] OpenAI API / Gemini API
- [ ] Spring AI

---

## 20. Glossary

| Term | Plain English Explanation |
|------|--------------------------|
| API | A set of rules for how programs talk to each other |
| REST | A style of API that uses HTTP methods (GET, POST, etc.) |
| JWT | A signed token that proves who you are without a database lookup |
| ORM | Software that converts database rows into Java objects automatically |
| Entity | A Java class that maps to a database table |
| Repository | A class that handles all database queries for one entity |
| Service | A class that contains business logic |
| Controller | A class that receives HTTP requests and returns HTTP responses |
| DTO | Data Transfer Object — a plain object used to send/receive data over the network |
| Migration | A script that modifies your database schema (tracked by Flyway) |
| ACID | Atomicity, Consistency, Isolation, Durability — guarantees of a reliable database |
| Transaction | A database operation that is all-or-nothing |
| Idempotency | Doing the same operation twice has the same effect as doing it once |
| Kafka | A message broker that routes events between services |
| Topic | A named channel in Kafka where events are published |
| Producer | A service that sends events to Kafka |
| Consumer | A service that reads and processes events from Kafka |
| Cache | Temporary fast storage for frequently accessed data |
| TTL | Time To Live — how long a cache entry is valid before expiring |
| Rate Limiting | Restricting how many requests a user can make in a time period |
| Feature Engineering | Transforming raw data into inputs a machine learning model can understand |
| Anomaly Detection | Finding data points that don't match the normal pattern |
| Sidecar | A separate small service that runs alongside the main app |
| Prompt Engineering | Writing instructions to a language model to get the desired output |
| DLQ | Dead Letter Queue — where failed messages go so they aren't lost |
| RBAC | Role-Based Access Control — different users have different permissions |
| bcrypt | A password hashing algorithm designed to be slow (resists brute force) |
| Optimistic Locking | Detect conflicts when saving rather than preventing them when reading |

---

## Quick Reference: Phase Completion Checklist

### Phase 1 Done When:
- [ ] POST /auth/register creates a user
- [ ] POST /auth/login returns a JWT
- [ ] POST /accounts creates a bank account
- [ ] POST /transfers moves money between accounts atomically
- [ ] Insufficient funds is rejected with a clear error

### Phase 2 Done When:
- [ ] Duplicate transfers (same idempotency key) return same result
- [ ] Transaction states (INITIATED → PROCESSING → COMPLETED) are tracked
- [ ] Stuck transactions are auto-recovered by scheduler

### Phase 3 Done When:
- [ ] Every transaction receives a fraud score
- [ ] HIGH/CRITICAL transactions create fraud cases
- [ ] CRITICAL transactions are auto-blocked

### Phase 4 Done When:
- [ ] Payment events flow through Kafka topics
- [ ] Fraud detection is triggered by Kafka consumer
- [ ] Failed messages go to DLQ without blocking others

### Phase 5 Done When:
- [ ] Balance reads are served from Redis cache
- [ ] Cache is invalidated after transfers
- [ ] Rate limiting blocks more than 10 transfers per minute per user

### Phase 6 Done When:
- [ ] Python ML service is running and returns fraud scores
- [ ] Java service calls ML service during fraud evaluation
- [ ] Combined rule + ML score is used for decision

### Phase 7 Done When:
- [ ] Analyst can ask questions in natural language via API
- [ ] AI retrieves live transaction data to answer questions
- [ ] Conversation history is maintained within a session

---

*This document is a living guide. Update it as you make progress.*
*Last updated: September 2026*
