package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public interface ExceptionStatus {
    HttpStatus getHttpStatus();
}
