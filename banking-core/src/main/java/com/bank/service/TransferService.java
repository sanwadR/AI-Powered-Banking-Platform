package com.bank.service;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.exception.AccountOperationException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * TransferService — the heart of the banking system.
 *
 * This service handles the most critical operation: moving money.
 * Every money transfer must be:
 *   - ATOMIC: All steps succeed or none do (database transaction)
 *   - VALIDATED: Enough balance, account active, not self-transfer
 *   - RECORDED: Every movement is logged permanently
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // ============================================================
    // TRANSFER MONEY
    // ============================================================

    /**
     * Transfer money between two accounts.
     *
     * @Transactional ensures everything happens in ONE database transaction.
     * If anything throws an exception, BOTH account updates are rolled back.
     * This guarantees we never lose money in transit.
     */
    @Transactional
    public TransactionResponse transfer(TransferRequest request, User requestingUser) {
        log.info("Transfer initiated: {} → {} for amount {}",
                request.getSourceAccountNumber(),
                request.getTargetAccountNumber(),
                request.getAmount());

        // ─── Step 1: Load both accounts ───────────────────────────────────
        Account sourceAccount = accountService.findAccountByNumber(
                request.getSourceAccountNumber());
        Account targetAccount = accountService.findAccountByNumber(
                request.getTargetAccountNumber());

        // ─── Step 2: Validate ─────────────────────────────────────────────

        // Ownership check: the source account must belong to the logged-in user
        if (!sourceAccount.getOwner().getId().equals(requestingUser.getId())) {
            throw new AccountOperationException(
                "You don't have permission to transfer from account: " +
                request.getSourceAccountNumber()
            );
        }

        // Cannot transfer to yourself
        if (sourceAccount.getId().equals(targetAccount.getId())) {
            throw new AccountOperationException("Cannot transfer money to the same account");
        }

        // Source account must be ACTIVE
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException(
                "Source account is not active. Status: " + sourceAccount.getStatus()
            );
        }

        // Target account must be ACTIVE
        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException(
                "Target account is not active. Status: " + targetAccount.getStatus()
            );
        }

        // Check sufficient funds
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds. Available: " + sourceAccount.getBalance() +
                " " + sourceAccount.getCurrency() +
                ", Requested: " + request.getAmount()
            );
        }

        // ─── Step 3: Create the transaction record (PENDING) ──────────────
        Transaction transaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .amount(request.getAmount())
                .currency(sourceAccount.getCurrency())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        // ─── Step 4: Deduct from source ───────────────────────────────────
        // subtract() returns a new BigDecimal, it doesn't modify the original
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(sourceAccount);

        // ─── Step 5: Add to target ────────────────────────────────────────
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));
        accountRepository.save(targetAccount);

        // ─── Step 6: Mark transaction as COMPLETED ────────────────────────
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        log.info("Transfer completed successfully. Reference: {}", transaction.getReferenceId());

        return TransactionResponse.from(transaction);
    }

    // ============================================================
    // GET TRANSACTION HISTORY
    // ============================================================

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(
            String accountNumber, User requestingUser, Pageable pageable) {

        Account account = accountService.findAccountByNumber(accountNumber);

        // Only the account owner can view its history
        if (!account.getOwner().getId().equals(requestingUser.getId())) {
            throw new AccountOperationException("You don't have access to this account");
        }

        return transactionRepository.findByAccount(account, pageable)
                .map(TransactionResponse::from);
    }

    // ============================================================
    // GET SINGLE TRANSACTION
    // ============================================================

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long transactionId, User requestingUser) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AccountOperationException(
                    "Transaction not found: " + transactionId
                ));

        // Security: only involved parties can view the transaction
        boolean isOwner = transaction.getSourceAccount().getOwner().getId()
                            .equals(requestingUser.getId())
                       || transaction.getTargetAccount().getOwner().getId()
                            .equals(requestingUser.getId());

        if (!isOwner) {
            throw new AccountOperationException("You don't have access to this transaction");
        }

        return TransactionResponse.from(transaction);
    }
}
