package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TransactionRepository — database operations for transactions.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find a transaction by its UUID reference ID.
     * This is what the client uses to look up a specific payment.
     */
    Optional<Transaction> findByReferenceId(UUID referenceId);

    /**
     * Get all transactions for an account (either as sender or receiver),
     * ordered by newest first.
     *
     * Page<Transaction> enables pagination: "give me page 1, 20 items per page".
     * This prevents loading thousands of transactions at once.
     */
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.sourceAccount = :account OR t.targetAccount = :account
        ORDER BY t.createdAt DESC
        """)
    Page<Transaction> findByAccount(Account account, Pageable pageable);

    /**
     * Get all transactions sent FROM a specific account.
     */
    List<Transaction> findBySourceAccountOrderByCreatedAtDesc(Account sourceAccount);

    /**
     * Find all transactions with a specific status.
     * Used by the scheduled job in Phase 2 to find stuck transactions.
     */
    List<Transaction> findByStatus(TransactionStatus status);
}
