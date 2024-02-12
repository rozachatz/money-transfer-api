package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class MoneyTransferException extends Exception implements HttpStatusProvider {

    public MoneyTransferException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return GlobalAPIExceptionHandler.GENERIC_ERROR_HTTP_STATUS;
    }
}