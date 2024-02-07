package com.moneytransfer.dto;

import com.moneytransfer.entity.Account;
import com.moneytransfer.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto that retains information for an {@link Account} entity.
 */
@Getter
@AllArgsConstructor
public class GetAccountDto {
    private UUID accountId;
    private BigDecimal balance;
    private Currency currency;
}


