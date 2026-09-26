package com.bank.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity — records every money movement permanently.
 *
 * IMPORTANT: Transactions are immutable once created.
 * We never UPDATE or DELETE transaction records in a real bank.
 * If something goes wrong, we create a new correcting transaction.
 * This is called an "append-only" ledger — the foundation of banking.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference ID: a globally unique identifier for this transaction.
     * Used by external systems (like the mobile app) to track a payment.
     * UUID guarantees uniqueness even without checking the database.
     */
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false)
    private UUID referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false)
    private String currency = "USD";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    /**
     * Type of transaction: TRANSFER, DEPOSIT, or WITHDRAWAL.
     * Maps to the transaction_type column added in V4 migration.
     */
    @Builder.Default
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType = TransactionType.TRANSFER;

    @Column
    private String description;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;  // Set when status becomes COMPLETED or FAILED

    @PrePersist
    protected void onCreate() {
        if (referenceId == null) referenceId = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        if (status == null) status = TransactionStatus.PENDING;
    }
}
