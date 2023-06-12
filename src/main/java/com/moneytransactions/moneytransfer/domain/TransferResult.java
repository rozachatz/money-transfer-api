package com.moneytransactions.moneytransfer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@AllArgsConstructor
@Getter
public class TransferResult{
        private UUID transactionId;
        private Long sourceAccountId;
        private Long targetAccountId;
        private BigDecimal amount;
        private LocalDateTime transferDateTime;
        private String message;
}