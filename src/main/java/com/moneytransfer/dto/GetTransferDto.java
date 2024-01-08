package com.moneytransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

/**
 * Dto for {@link com.moneytransfer.entity.Transaction}
 */
@Getter
@AllArgsConstructor
public class GetTransferDto {
    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private BigDecimal amount;
}


