package com.moneytransactions.moneytransfer.exceptions;

public class SameAccountException extends MoneyTransferException {
    public SameAccountException(String message) {
        super(message);
    }

}
