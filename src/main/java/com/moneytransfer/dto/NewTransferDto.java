package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * This record models the information for representing a new {@link Transaction} request.
 *
 * @param sourceAccountId
 * @param targetAccountId
 * @param amount
 */
public record NewTransferDto(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
}
