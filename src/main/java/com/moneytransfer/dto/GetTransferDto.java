package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto that retains information for a {@link Transaction} entity.
 */
@Getter
@AllArgsConstructor
public class GetTransferDto {
    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private BigDecimal amount;
    private RequestStatus status;
    private Currency currency;
}


