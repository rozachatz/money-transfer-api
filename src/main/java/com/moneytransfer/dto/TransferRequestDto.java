package com.moneytransfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDto(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID request_id) {
}
