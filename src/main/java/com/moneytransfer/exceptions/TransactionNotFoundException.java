package com.moneytransfer.exceptions;

public class TransactionNotFoundException extends MoneyTransferException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
