package com.bank.controller;

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.DepositRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.TransactionResponse;
import com.bank.model.User;
import com.bank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AccountController — HTTP endpoints for bank account management.
 *
 * @AuthenticationPrincipal User currentUser
 * → Spring injects the currently logged-in User object automatically.
 *   Spring Security puts the User in the SecurityContext (in JwtAuthFilter).
 *   @AuthenticationPrincipal extracts it from there.
 *   We never have to manually parse the JWT in controllers.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management")
@SecurityRequirement(name = "bearerAuth")   // Tell Swagger this endpoint needs a JWT
public class AccountController {

    private final AccountService accountService;

    /**
     * POST /api/v1/accounts
     * Open a new bank account for the logged-in user.
     */
    @PostMapping
    @Operation(summary = "Open a new bank account (SAVINGS or CHECKING)")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        AccountResponse response = accountService.createAccount(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/accounts
     * List all accounts belonging to the logged-in user.
     */
    @GetMapping
    @Operation(summary = "Get all my bank accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal User currentUser
    ) {
        List<AccountResponse> accounts = accountService.getMyAccounts(currentUser);
        return ResponseEntity.ok(accounts);
    }

    /**
     * GET /api/v1/accounts/{accountNumber}
     * Get details of a specific account.
     *
     * @PathVariable maps the {accountNumber} part of the URL to the method parameter.
     */
    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get details of a specific account")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal User currentUser
    ) {
        AccountResponse response = accountService.getAccount(accountNumber, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/accounts/{accountNumber}
     * Close an account (only if balance is zero).
     */
    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Close a bank account (requires zero balance)")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal User currentUser
    ) {
        AccountResponse response = accountService.closeAccount(accountNumber, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/accounts/{accountNumber}/deposit
     * Deposit money into an account.
     *
     * This creates a real transaction: BANK_RESERVE → your account.
     * Every deposit is fully traceable in the transaction history.
     *
     * Example request body:
     * {
     *   "amount": 5000.00,
     *   "description": "Initial deposit"
     * }
     */
    @PostMapping("/{accountNumber}/deposit")
    @Operation(summary = "Deposit money into your account (creates a traceable transaction)")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TransactionResponse response = accountService.deposit(accountNumber, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
