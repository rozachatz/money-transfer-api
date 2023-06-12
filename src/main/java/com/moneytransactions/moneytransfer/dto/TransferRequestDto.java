package com.moneytransactions.moneytransfer.dto;

import java.math.BigDecimal;

public record TransferRequestDto(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
}
