package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service that performs the financial Transactions
 */
public interface TransactionService {
    Transaction transferOptimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction transferPessimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    TransferAccountsDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    Transaction getTransactionById(UUID id) throws ResourceNotFoundException;

    Account getAccountById(UUID id) throws ResourceNotFoundException;

    List<Transaction> getTransactionByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException;

    Page<Account> getAccountsWithLimit(int limit);

    Transaction initiateTransfer(TransferAccountsDto transferAccountsDto, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    Transaction transferSerializable(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

}
