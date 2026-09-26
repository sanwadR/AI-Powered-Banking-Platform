package com.bank.exception;

// Custom exception classes — each represents a specific error scenario.
// Extending RuntimeException means they're unchecked (no need to declare throws).

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
