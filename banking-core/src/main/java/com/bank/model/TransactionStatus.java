package com.bank.model;

public enum TransactionStatus {
    PENDING,     // Just created, not yet processed
    COMPLETED,   // Money moved successfully
    FAILED       // Something went wrong, money NOT moved
}
