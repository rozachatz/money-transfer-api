package com.moneytransactions.moneytransfer.exceptions;

public class InsufficientBalanceException extends MoneyTransferException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
