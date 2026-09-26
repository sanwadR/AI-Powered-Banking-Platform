package com.bank.dto.response;

import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TransactionResponse — what we return after a transfer or when listing transactions.
 */
@Data
@Builder
public class TransactionResponse {
    private Long id;
    private UUID referenceId;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private TransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TransactionResponse from(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .referenceId(txn.getReferenceId())
                .sourceAccountNumber(txn.getSourceAccount().getAccountNumber())
                .targetAccountNumber(txn.getTargetAccount().getAccountNumber())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .transactionType(txn.getTransactionType())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .completedAt(txn.getCompletedAt())
                .build();
    }
}
