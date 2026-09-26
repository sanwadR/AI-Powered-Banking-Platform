package com.bank.model;

/**
 * Enum for user roles.
 *
 * An enum is a fixed set of constants.
 * This is safer than using plain Strings ("CUSTOMER", "ADMIN")
 * because typos would cause a compile error instead of a bug.
 */
public enum Role {
    CUSTOMER,       // Regular bank customer
    SUPPORT,        // Customer support staff (read-only access)
    FRAUD_ANALYST,  // Can review fraud cases and use AI investigator
    ADMIN           // Full access to everything
}
