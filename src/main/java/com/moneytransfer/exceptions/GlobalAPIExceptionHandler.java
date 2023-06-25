package com.moneytransfer.exceptions;

import com.moneytransfer.dto.ErrorResponseDto;
import com.moneytransfer.service.TransactionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalAPIExceptionHandler {
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    @ExceptionHandler(MoneyTransferException.class)
    public ResponseEntity<ErrorResponseDto> handleMoneyExceptions(MoneyTransferException e) {
        HttpStatus status = determineHttpStatus(e);
        ErrorResponseDto errorResponse = new ErrorResponseDto(status.value(), e.getMessage());
        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    private HttpStatus determineHttpStatus(MoneyTransferException e) {
        return e.getHttpStatus();
    }
}
