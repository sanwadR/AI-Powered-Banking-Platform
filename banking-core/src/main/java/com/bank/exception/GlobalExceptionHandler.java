package com.bank.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — catches ALL exceptions from ALL controllers.
 *
 * Without this, unhandled exceptions return a generic Spring Boot error page.
 * With this, we return structured, informative JSON error responses.
 *
 * @RestControllerAdvice = intercepts exceptions thrown from any @RestController.
 * @ExceptionHandler(XxxException.class) = handle this specific exception type.
 *
 * Example response:
 * {
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "email: Must be a valid email address",
 *   "timestamp": "2024-12-01T14:30:00"
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Validation errors (e.g., @NotBlank, @Email failed) ──────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse.builder()
                .status(400)
                .error("Validation Failed")
                .message(fieldErrors.toString())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // ─── Email already in use ─────────────────────────────────────────────
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        log.warn("Email conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Email Already Exists", ex.getMessage());
    }

    // ─── Account not found ────────────────────────────────────────────────
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Account Not Found", ex.getMessage());
    }

    // ─── Insufficient funds ───────────────────────────────────────────────
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient Funds", ex.getMessage());
    }

    // ─── Account operation not allowed ────────────────────────────────────
    @ExceptionHandler(AccountOperationException.class)
    public ResponseEntity<ErrorResponse> handleAccountOperation(AccountOperationException ex) {
        log.warn("Account operation error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Operation Not Allowed", ex.getMessage());
    }

    // ─── Wrong email or password ──────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Failed login attempt");
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials",
            "Email or password is incorrect");
    }

    // ─── Not authorized (wrong role) ──────────────────────────────────────
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access Denied",
            "You don't have permission to perform this action");
    }

    // ─── Catch-all for unexpected errors ──────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "An unexpected error occurred. Please try again later.");
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(
            ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // ─── Error response shape ─────────────────────────────────────────────
    @lombok.Builder
    @lombok.Data
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
    }
}
