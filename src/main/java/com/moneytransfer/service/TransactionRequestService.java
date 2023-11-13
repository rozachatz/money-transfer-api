package com.moneytransfer.service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service that processes a TransactionRequest
 */
public interface TransactionRequestService {
    Transaction processRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID requestId) throws MoneyTransferException;
}
