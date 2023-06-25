package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends MoneyTransferException implements ExceptionStatus {
    public InsufficientBalanceException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.PAYMENT_REQUIRED;
    }
}
