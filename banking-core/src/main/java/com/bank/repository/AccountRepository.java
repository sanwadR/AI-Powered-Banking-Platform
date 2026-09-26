package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AccountRepository — database operations for bank accounts.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts belonging to a specific user.
     * SQL: SELECT * FROM accounts WHERE owner_id = ?
     */
    List<Account> findByOwner(User owner);

    /**
     * Find all active accounts belonging to a user.
     * SQL: SELECT * FROM accounts WHERE owner_id = ? AND status = ?
     */
    List<Account> findByOwnerAndStatus(User owner, AccountStatus status);

    /**
     * Find an account by its account number (e.g., "ACC-20241201-000001").
     * SQL: SELECT * FROM accounts WHERE account_number = ?
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Check if an account number already exists (to prevent duplicates).
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find an account by ID and lock it for update.
     *
     * PESSIMISTIC_WRITE lock means: "Lock this row in the database.
     * No other transaction can update it until I'm done."
     *
     * Used in transfer service to prevent race conditions when
     * multiple transfers try to modify the same account simultaneously.
     *
     * @Query lets us write custom JPQL (similar to SQL but uses entity names)
     */
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(Long id);
}
