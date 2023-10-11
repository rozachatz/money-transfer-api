package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class SameAccountException extends MoneyTransferException {
    public SameAccountException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
