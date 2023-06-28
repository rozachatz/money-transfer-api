package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class SameAccountException extends MoneyTransferException implements HttpStatusProvider {
    public SameAccountException(String message) {
        super(message);
    }
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
