package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.TransactionNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {
    Transaction transferOptimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction transferPessimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;

    TransferAccountsDto getAccountsByIdsPessimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;

    TransferAccountsDto getAccountsByIdsOptimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException;

    Transaction getTransactionById(UUID id) throws TransactionNotFoundException;
}
