package com.bank.model;

/**
 * TransactionType — what kind of money movement is this?
 *
 * TRANSFER   → Customer sends money to another customer account
 * DEPOSIT    → Money enters the system (from BANK_RESERVE → customer)
 * WITHDRAWAL → Money leaves the system (customer → BANK_RESERVE)
 *              [Withdrawal will be built in a future update]
 */
public enum TransactionType {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL
}
