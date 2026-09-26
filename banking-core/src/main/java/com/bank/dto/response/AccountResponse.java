package com.bank.dto.response;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AccountResponse — what we send back when a client asks about an account.
 *
 * We NEVER send the Account entity directly.
 * Why? The entity contains sensitive internal fields (version, JPA metadata).
 * We control exactly what goes into the response using DTOs.
 */
@Data
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String ownerName;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;

    /**
     * Factory method: convert an Account entity to an AccountResponse DTO.
     * This keeps the mapping logic in one place.
     */
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerName(account.getOwner().getFullName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
