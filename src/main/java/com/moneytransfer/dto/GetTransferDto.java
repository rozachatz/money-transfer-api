package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto that retains information for a {@link Transaction} entity.
 */
public record GetTransferDto (UUID transactionId, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, TransactionStatus status, Currency currency){
}


