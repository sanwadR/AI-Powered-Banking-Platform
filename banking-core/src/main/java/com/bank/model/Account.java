package com.bank.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account entity — maps to the "accounts" table.
 *
 * BigDecimal is used for money, NOT double or float.
 * Why? Because floating point numbers have precision errors.
 * Example: 0.1 + 0.2 = 0.30000000000000004 in floating point.
 * BigDecimal stores exact values, which is essential for banking.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;   // e.g. ACC-20241201-000001

    /**
     * ManyToOne: Many accounts can belong to one user.
     * LAZY loading = don't fetch the User from DB unless we specifically ask for it.
     * This is a performance optimization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Builder.Default
    @Column(nullable = false)
    private String currency = "USD";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    /**
     * Version field for Optimistic Locking (Phase 2).
     * @Version tells JPA: "when updating, check this number hasn't changed."
     * If it has changed (someone else updated first), throw OptimisticLockException.
     */
    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = AccountStatus.ACTIVE;
        if (balance == null) balance = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
