package com.moneytransfer.exceptions;

import com.moneytransfer.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalAPIExceptionHandler {
    @ExceptionHandler(MoneyTransferException.class)
    public ResponseEntity<ErrorResponseDto> handleMoneyExceptions(MoneyTransferException e) {
        HttpStatus status = e.getHttpStatus();
        ErrorResponseDto errorResponse = new ErrorResponseDto(status.value(), e.getMessage());
        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

}
