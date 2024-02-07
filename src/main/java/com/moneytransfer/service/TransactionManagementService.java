package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Type;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service that processes a TransactionRequest and fetches Transactions
 */
public interface TransactionManagementService {
    Transaction processRequest(NewTransferDto transferDto, UUID transferRequestId, Type type) throws MoneyTransferException;

    Transaction getTransactionById(UUID requestId) throws ResourceNotFoundException;

    List<Transaction> getTransactionByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException;

    Page<Transaction> getTransactionsWithLimit(final int limit);

}
