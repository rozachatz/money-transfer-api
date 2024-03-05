package com.moneytransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Dto for logging and exception handling
 */
public record ErrorResponseDto (int HttpStatusValue, String message){
}