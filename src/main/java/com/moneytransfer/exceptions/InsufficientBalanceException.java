package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends MoneyTransferException implements HttpStatusProvider {
    public InsufficientBalanceException(String message) {
        super(message);
    }
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.PAYMENT_REQUIRED;
    }
}
