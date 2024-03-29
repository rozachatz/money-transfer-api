package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class RequestConflictException extends MoneyTransferException {
    public RequestConflictException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

}
