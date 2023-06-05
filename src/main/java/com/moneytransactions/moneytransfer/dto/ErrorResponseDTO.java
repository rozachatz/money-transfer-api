package com.moneytransactions.moneytransfer.dto;

import lombok.Getter;

@Getter
public class ErrorResponseDTO {
    private int status;
    private String message;

    public ErrorResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
    }

}