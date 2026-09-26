package com.bank.controller;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.model.User;
import com.bank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * TransferController — HTTP endpoints for money transfers.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Money transfer and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    /**
     * POST /api/v1/transfers
     * Initiate a money transfer between accounts.
     */
    @PostMapping("/transfers")
    @Operation(summary = "Transfer money between accounts")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TransactionResponse response = transferService.transfer(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/transfers/{id}
     * Get details of a specific transaction.
     */
    @GetMapping("/transfers/{id}")
    @Operation(summary = "Get transaction details by ID")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        TransactionResponse response = transferService.getTransaction(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/accounts/{accountNumber}/transactions
     * Get paginated transaction history for an account.
     *
     * Query params (optional):
     *   ?page=0&size=20  → page number and items per page
     */
    @GetMapping("/accounts/{accountNumber}/transactions")
    @Operation(summary = "Get paginated transaction history for an account")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        // Sort by newest first
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponse> transactions = transferService.getTransactionHistory(
                accountNumber, currentUser, pageable);
        return ResponseEntity.ok(transactions);
    }
}
