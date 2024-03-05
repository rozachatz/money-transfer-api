package com.moneytransfer.service;

import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;

/**
 * Service for exchanging source currency in a transfer operation
 */
public interface CurrencyExchangeService {
    BigDecimal exchangeCurrency(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency) throws MoneyTransferException;
}
