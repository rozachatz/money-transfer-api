package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * This record models the information for representing a new {@link Transaction} request.
 *
 * @param sourceAccountId
 * @param targetAccountId
 * @param amount
 */
public record NewTransferDto(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
    @Override
    public int hashCode() {
        return Objects.hash(sourceAccountId, targetAccountId, amount.stripTrailingZeros());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NewTransferDto that = (NewTransferDto) obj;
        return Objects.equals(sourceAccountId, that.sourceAccountId) &&
                Objects.equals(targetAccountId, that.targetAccountId) &&
                Objects.equals(amount, that.amount);
    }
}
