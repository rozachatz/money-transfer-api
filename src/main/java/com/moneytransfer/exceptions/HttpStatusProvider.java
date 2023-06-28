package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public interface HttpStatusProvider {
    HttpStatus getHttpStatus();
}
