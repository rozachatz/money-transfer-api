package com.moneytransfer.service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for {@link GetTransactionService}.
 */
@Service
@RequiredArgsConstructor
class GetTransactionServiceImpl implements GetTransactionService {
    /**
     * The transaction repository.
     */
    private final TransactionRepository transactionRepository;

    /**
     * Returns a list of {@link Transaction} entities with amount in the given range.
     *
     * @param minAmount
     * @param maxAmount
     * @return transactions
     * @throws ResourceNotFoundException
     */
    public List<Transaction> getTransactionByAmountBetween(final BigDecimal minAmount, final BigDecimal maxAmount) throws ResourceNotFoundException {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount)
                .orElseThrow(() -> {
                    var errorMessage = "Transactions within the specified range: [" + minAmount + "," + maxAmount + "] were not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Gets all transactions with limited number of results.
     *
     * @param limit
     * @return Accounts
     */
    public Page<Transaction> getTransactionsWithLimit(final int limit) {
        var pageRequest = PageRequest.of(0, limit);
        return transactionRepository.findAll(pageRequest);
    }

    /**
     * Gets {@link Transaction} by id.
     *
     * @param transactionId
     * @return Transaction
     * @throws ResourceNotFoundException
     */
    public Transaction getTransactionById(final UUID transactionId) throws ResourceNotFoundException {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    var errorMessage = "Transaction with id: " + transactionId + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

}
