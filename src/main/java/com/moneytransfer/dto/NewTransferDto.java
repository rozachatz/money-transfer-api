package com.moneytransfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * This record models the information needed to create a new {@link com.moneytransfer.entity.Transaction} in the system.
 * @param sourceAccountId
 * @param targetAccountId
 * @param amount
 */
public record NewTransferDto(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
}
