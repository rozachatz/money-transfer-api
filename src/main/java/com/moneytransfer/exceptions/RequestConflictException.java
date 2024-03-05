package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class RequestConflictException extends MoneyTransferException {
    private final HttpStatus httpStatus;

    public RequestConflictException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public RequestConflictException(String message) {
        super(message);
        this.httpStatus = HttpStatus.CONFLICT;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
