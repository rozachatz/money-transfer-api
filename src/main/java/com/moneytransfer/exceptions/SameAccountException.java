package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class SameAccountException extends MoneyTransferException implements ExceptionStatus {
    public SameAccountException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
