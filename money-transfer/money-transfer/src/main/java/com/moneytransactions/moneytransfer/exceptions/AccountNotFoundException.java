package com.moneytransactions.moneytransfer.exceptions;

public class AccountNotFoundException extends MoneyTransferException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
