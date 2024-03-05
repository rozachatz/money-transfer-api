package com.moneytransfer.enums;

/**
 * Concurrency control for the money transfer.
 */
public enum ConcurrencyControlMode {
    OPTIMISTIC_LOCKING,
    PESSIMISTIC_LOCKING,
    SERIALIZABLE_ISOLATION

}
