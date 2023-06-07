package com.moneytransactions.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TransferDTO {
    @JsonProperty("transactionId")
    private UUID transactionId;
    @JsonProperty("sourceAccountId")
    private Long sourceAccountId;
    @JsonProperty("targetAccountId")
    private Long targetAccountId;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("transferDateTime")
    private LocalDateTime transferDateTime;
    @JsonProperty("message")
    private String message;

    public TransferDTO(UUID transactionId, Long sourceAccountId, Long targetAccountId, BigDecimal amount, LocalDateTime transferDateTime, String message) {
        this.transactionId = transactionId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.transferDateTime = transferDateTime;
        this.message = message;
    }
}


