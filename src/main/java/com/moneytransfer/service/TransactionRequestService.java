package com.moneytransfer.service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionRequestService {
    Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException;
    Transaction processRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID requestId) throws MoneyTransferException;
}
