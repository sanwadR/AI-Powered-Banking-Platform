package com.bank.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DepositRequest — the JSON body for depositing money into an account.
 *
 * Example request body:
 * {
 *   "amount": 5000.00,
 *   "description": "Initial deposit"
 * }
 */
@Data
public class DepositRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Deposit amount must be at least 0.01")
    private BigDecimal amount;

    private String description = "Deposit";   // Optional note, defaults to "Deposit"
}
