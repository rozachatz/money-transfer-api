package com.moneytransfer.dto;

import com.moneytransfer.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto for {@link com.moneytransfer.entity.Account}
 */
@Getter
@AllArgsConstructor
public class GetAccountDto {
    private UUID accountId;
    private BigDecimal balance;
    private Currency currency;
}


