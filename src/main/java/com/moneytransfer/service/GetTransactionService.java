package com.moneytransfer.service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service that gets {@link Transaction} entities.
 */
public interface GetTransactionService {

    Transaction getTransactionById(UUID transactionId) throws ResourceNotFoundException;

    List<Transaction> getTransactionByAmountBetween(BigDecimal minAmount,
                                                    BigDecimal maxAmount) throws ResourceNotFoundException;

    Page<Transaction> getTransactionsWithLimit(final int limit);

}
