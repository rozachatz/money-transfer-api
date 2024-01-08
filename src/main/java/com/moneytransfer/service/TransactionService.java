package com.moneytransfer.service;

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
    Transaction transferSerializable(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction transferOptimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction transferPessimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;

    Transaction getTransactionById(UUID id) throws ResourceNotFoundException;

    Account getAccountById(UUID id) throws ResourceNotFoundException;

    List<Transaction> getTransactionByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException;

    Page<Account> getAccountsWithLimit(int limit);
}
