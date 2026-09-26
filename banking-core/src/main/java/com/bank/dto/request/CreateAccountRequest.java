package com.bank.dto.request;

import com.bank.model.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * CreateAccountRequest — body for opening a new bank account.
 */
@Data
public class CreateAccountRequest {

    @NotNull(message = "Account type is required (SAVINGS or CHECKING)")
    private AccountType accountType;

    private String currency = "USD";   // Default to USD if not provided
}
