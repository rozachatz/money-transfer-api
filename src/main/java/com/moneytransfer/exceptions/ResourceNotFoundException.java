package com.moneytransfer.exceptions;

public class ResourceNotFoundException extends MoneyTransferException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
