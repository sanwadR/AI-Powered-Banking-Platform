package com.bank.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * TransferRequest — body for initiating a money transfer.
 *
 * Note: We use account NUMBERS (not IDs) in the API.
 * Why? Account numbers are what users know ("my account is ACC-000001").
 * IDs are internal database implementation details that should stay hidden.
 */
@Data
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;

    @NotBlank(message = "Target account number is required")
    private String targetAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    private String description;   // Optional memo/note
}
