package com.moneytransactions.moneytransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetTransferDto {
    private UUID transactionId;
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
}


