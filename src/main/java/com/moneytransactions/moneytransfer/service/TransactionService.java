package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.dto.TransferAccountsDto;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;

public interface TransactionService {
    Transaction transferFundsOptimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;
    TransferAccountsDto getAccountsByIdsOptimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;
    Transaction transferFundsPessimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;
    TransferAccountsDto getAccountsByIdsPessimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;
}
