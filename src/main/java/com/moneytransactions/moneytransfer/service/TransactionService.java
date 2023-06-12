package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.dto.TransferAccountsDto;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.TransactionNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {
    Transaction transferOptimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;
    Transaction transferPessimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;

    TransferAccountsDto getAccountsByIdsPessimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;
    TransferAccountsDto getAccountsByIdsOptimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;
    Transaction getTransactionById(UUID id) throws TransactionNotFoundException;
}
