package com.bank.service;

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.DepositRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.TransactionResponse;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.AccountOperationException;
import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AccountService — business logic for bank accounts.
 *
 * This service enforces rules like:
 *   - You can only close an account with zero balance
 *   - Account numbers must be unique
 *   - You can only view your own accounts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Fixed account number for the bank's reserve account (created in V4 migration)
    private static final String BANK_RESERVE_ACCOUNT = "BANK-RESERVE-001";

    // Counter for generating unique account numbers within the same second
    // AtomicLong is thread-safe (works correctly even with concurrent requests)
    private final java.util.concurrent.atomic.AtomicLong accountNumberCounter = new java.util.concurrent.atomic.AtomicLong(1);

    // ============================================================
    // CREATE ACCOUNT
    // ============================================================

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, User owner) {
        log.info("Creating {} account for user: {}", request.getAccountType(), owner.getEmail());

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .owner(owner)
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)   // All accounts start with zero balance
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getAccountNumber());

        return AccountResponse.from(saved);
    }

    // ============================================================
    // GET MY ACCOUNTS
    // ============================================================

    @Transactional(readOnly = true)  // readOnly = optimization for SELECT-only operations
    public List<AccountResponse> getMyAccounts(User owner) {
        return accountRepository.findByOwner(owner)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    // ============================================================
    // GET ACCOUNT BY NUMBER
    // ============================================================

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber, User requestingUser) {
        Account account = findAccountByNumber(accountNumber);

        // Security check: only the owner can view their account
        if (!account.getOwner().getId().equals(requestingUser.getId())) {
            throw new AccountOperationException("You don't have access to this account");
        }

        return AccountResponse.from(account);
    }

    // ============================================================
    // CLOSE ACCOUNT
    // ============================================================

    @Transactional
    public AccountResponse closeAccount(String accountNumber, User requestingUser) {
        Account account = findAccountByNumber(accountNumber);

        // Security: only owner can close their account
        if (!account.getOwner().getId().equals(requestingUser.getId())) {
            throw new AccountOperationException("You don't have access to this account");
        }

        // Business rule: cannot close account with remaining balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountOperationException(
                "Cannot close account with remaining balance of " +
                account.getBalance() + " " + account.getCurrency() +
                ". Please transfer the funds first."
            );
        }

        // Business rule: cannot close an already closed account
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountOperationException("Account is already closed");
        }

        account.setStatus(AccountStatus.CLOSED);
        Account saved = accountRepository.save(account);
        log.info("Account closed: {}", accountNumber);

        return AccountResponse.from(saved);
    }

    // ============================================================
    // DEPOSIT MONEY
    // ============================================================

    /**
     * Deposit money into a customer account.
     *
     * How it works:
     *   1. The BANK_RESERVE account is the "source" of all deposits
     *   2. We create a real transaction record (BANK_RESERVE → customer)
     *   3. We deduct from the reserve and add to the customer account
     *   4. Both updates are atomic — all-or-nothing
     *
     * This ensures every balance change has a matching transaction record.
     * No money appears from nowhere.
     */
    @Transactional
    public TransactionResponse deposit(String accountNumber, DepositRequest request, User requestingUser) {
        log.info("Deposit of {} requested for account: {}", request.getAmount(), accountNumber);

        // Load the customer's account
        Account targetAccount = findAccountByNumber(accountNumber);

        // Security: only the account owner can deposit into their own account
        if (!targetAccount.getOwner().getId().equals(requestingUser.getId())) {
            throw new AccountOperationException(
                "You don't have permission to deposit into account: " + accountNumber
            );
        }

        // Account must be ACTIVE to receive deposits
        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException(
                "Cannot deposit into account with status: " + targetAccount.getStatus()
            );
        }

        // Load the bank reserve account (the source of all deposits)
        Account bankReserve = accountRepository.findByAccountNumber(BANK_RESERVE_ACCOUNT)
                .orElseThrow(() -> new AccountOperationException(
                    "Bank reserve account not found. Please ensure V4 migration ran successfully."
                ));

        // Build and save the transaction record BEFORE modifying balances
        // This ensures if balance updates fail, we have a PENDING record to investigate
        Transaction depositTxn = Transaction.builder()
                .sourceAccount(bankReserve)
                .targetAccount(targetAccount)
                .amount(request.getAmount())
                .currency(targetAccount.getCurrency())
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.DEPOSIT)
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .build();

        depositTxn = transactionRepository.save(depositTxn);

        // Deduct from bank reserve
        bankReserve.setBalance(bankReserve.getBalance().subtract(request.getAmount()));
        accountRepository.save(bankReserve);

        // Credit the customer account
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));
        accountRepository.save(targetAccount);

        // Mark transaction as COMPLETED
        depositTxn.setStatus(TransactionStatus.COMPLETED);
        depositTxn.setCompletedAt(java.time.LocalDateTime.now());
        depositTxn = transactionRepository.save(depositTxn);

        log.info("Deposit of {} completed for account: {}. New balance: {}",
                request.getAmount(), accountNumber, targetAccount.getBalance());

        return TransactionResponse.from(depositTxn);
    }

    // ============================================================
    // HELPERS (package-private so TransferService can use them)
    // ============================================================

    Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                    "Account not found: " + accountNumber
                ));
    }

    /**
     * Generate a unique account number in format: ACC-YYYYMMDD-NNNNNN
     * Example: ACC-20241201-000001
     */
    private String generateAccountNumber() {
        String datePart = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = accountNumberCounter.getAndIncrement();
        String accountNumber = String.format("ACC-%s-%06d", datePart, counter);

        // If this number already exists (restart scenario), increment and try again
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            counter = accountNumberCounter.getAndIncrement();
            accountNumber = String.format("ACC-%s-%06d", datePart, counter);
        }

        return accountNumber;
    }
}
