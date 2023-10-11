package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {
    Transaction transferOptimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction transferPessimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    TransferAccountsDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    Transaction getTransactionById(UUID id) throws ResourceNotFoundException;

    Account getAccountById(UUID id) throws ResourceNotFoundException;

    TransactionRequest getOrCreateTransactionRequest(UUID requestId);

    Transaction processRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID requestId) throws MoneyTransferException;
}
