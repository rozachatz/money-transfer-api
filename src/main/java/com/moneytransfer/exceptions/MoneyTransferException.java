package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class MoneyTransferException extends Exception implements ExceptionStatus {
    public MoneyTransferException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


}