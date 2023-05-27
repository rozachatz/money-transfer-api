package com.moneytransactions.moneytransfer.dto;
import java.math.BigDecimal;

public record TransferRequest(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
}
