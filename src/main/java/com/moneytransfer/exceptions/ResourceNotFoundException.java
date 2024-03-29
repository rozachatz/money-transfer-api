package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends MoneyTransferException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
