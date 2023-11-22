package com.moneytransfer.service;

import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;

public interface CurrencyExchangeService {
    BigDecimal exchangeCurrency(double amount, Currency targetCurrency, Currency sourceCurrency) throws MoneyTransferException;
}
