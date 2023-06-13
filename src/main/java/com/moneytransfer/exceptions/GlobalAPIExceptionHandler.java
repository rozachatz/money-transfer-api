package com.moneytransfer.exceptions;

import com.moneytransfer.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalAPIExceptionHandler {
    @ExceptionHandler(MoneyTransferException.class)
    public ResponseEntity<ErrorResponseDto> handleMoneyExceptions(MoneyTransferException e) {
        HttpStatus status = determineHttpStatus(e);
        ErrorResponseDto errorResponse = new ErrorResponseDto(status.value(), e.getMessage());
        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    private HttpStatus determineHttpStatus(MoneyTransferException e) {
        if (e instanceof InsufficientBalanceException) {
            return HttpStatus.PAYMENT_REQUIRED;
        } else if (e instanceof SameAccountException) {
            return HttpStatus.BAD_REQUEST;
        } else if (e instanceof AccountNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (e instanceof TransactionNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
