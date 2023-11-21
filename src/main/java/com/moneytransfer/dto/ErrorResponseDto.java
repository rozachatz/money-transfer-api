package com.moneytransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Dto for Exceptions
 */
@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private int HttpStatusValue;
    private String message;
}