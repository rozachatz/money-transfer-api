package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends MoneyTransferException implements ExceptionStatus {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
