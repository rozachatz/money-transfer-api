package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.domain.TransferResult;
import com.moneytransactions.moneytransfer.dto.GetTransferDto;
import com.moneytransactions.moneytransfer.dto.TransferAccountsDto;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;

public interface TransactionService {
    TransferResult transferFundsOptimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;
    TransferAccountsDto getAccountsByIdsOptimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;

    TransferResult transferFundsPessimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;
    TransferAccountsDto getAccountsByIdsPessimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;
}
