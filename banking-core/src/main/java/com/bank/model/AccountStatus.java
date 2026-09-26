package com.bank.model;

public enum AccountStatus {
    ACTIVE,    // Normal, fully operational
    FROZEN,    // Temporarily restricted (e.g., suspected fraud)
    CLOSED     // Permanently closed
}
