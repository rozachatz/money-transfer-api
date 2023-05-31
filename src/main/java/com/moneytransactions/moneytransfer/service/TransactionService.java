package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    void moneyTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;

    List<Account> validateTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;
}
