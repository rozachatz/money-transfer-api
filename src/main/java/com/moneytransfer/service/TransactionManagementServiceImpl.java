package com.moneytransfer.service;

import com.moneytransfer.component.BuildHashedPayloadFunction;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.enums.Type;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for {@link TransactionManagementService}.
 */
@Service
@RequiredArgsConstructor
public class TransactionManagementServiceImpl implements TransactionManagementService {
    /**
     * The transaction repository.
     */
    private final TransactionRepository transactionRepository;
    /**
     * The money  transfer service.
     */
    private final MoneyTransferService moneyTransferService;
    /**
     * The account management service.
     */
    private final AccountManagementService accountManagementService;
    /**
     * The function for building hashed payload.
     */
    private final BuildHashedPayloadFunction buildHashedPayloadFunction;

    /**
     * Processes the TransactionRequest.
     *
     * @param newTransferDto
     * @param requestId
     * @return Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction processRequest(final NewTransferDto newTransferDto, final UUID requestId, final Type type) throws MoneyTransferException {
        RequestStatus status = getTransactionStatus(requestId);
        return switch (status) {
            case IN_PROGRESS -> processInProgressRequest(requestId, newTransferDto, type);
            case SUCCESS -> {
                Transaction transaction = getTransactionById(requestId);
                validateIdempotent(transaction, newTransferDto);
                yield transaction;
            }
            case FAILED -> {
                Transaction transaction = getTransactionById(requestId);
                validateIdempotent(transaction, newTransferDto);
                throw new RequestConflictException(transaction.getMessage());
            }
        };
    }

    /**
     * Returns all transactions within the given amount range.
     *
     * @param minAmount
     * @param maxAmount
     * @return Transactions
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
     * Gets {@link Transaction} by requestId.
     *
     * @param requestId
     * @return Transaction
     * @throws ResourceNotFoundException
     */

    public Transaction getTransactionById(final UUID requestId) throws ResourceNotFoundException {
        return transactionRepository.findById(requestId)
                .orElseThrow(() -> {
                    var errorMessage = "Transaction with requestId: " + requestId + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Get {@link RequestStatus} if {@link Transaction} exists or return in_progress status.
     *
     * @param requestId
     * @return
     */
    private RequestStatus getTransactionStatus(final UUID requestId) {
        return transactionRepository.findById(requestId).map(Transaction::getStatus).orElse(RequestStatus.IN_PROGRESS);
    }

    /**
     * Process the {@link Transaction} in progress
     *
     * @param requestId
     * @param newTransferDto
     * @param type
     * @return
     * @throws MoneyTransferException
     */
    private Transaction processInProgressRequest(final UUID requestId, final NewTransferDto newTransferDto, final Type type) throws MoneyTransferException {
        try {
            return persistSuccessfulTransaction(requestId, type, newTransferDto);
        } catch (MoneyTransferException | RuntimeException e) {
            String errorMessage = getErrorMessage(e);
            persistFailedTransaction(requestId, newTransferDto, errorMessage);
            throw e;
        }
    }

    /**
     * Gets the error message for the {@link Transaction}.
     *
     * @param e
     * @return
     */
    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        if (e instanceof ConcurrencyFailureException) {
            message = "Concurrent modification error: Another transaction has modified the account resources concurrently.";
        }
        return message;
    }

    /**
     * Persists the successful {@link Transaction}.
     *
     * @param requestId
     * @param type
     * @param newTransferDto
     * @return
     * @throws MoneyTransferException
     */
    private Transaction persistSuccessfulTransaction(final UUID requestId, final Type type, final NewTransferDto newTransferDto) throws MoneyTransferException {
        return switch (type) {
            case OPTIMISTIC_LOCKING -> moneyTransferService.transferOptimistic(requestId, newTransferDto);
            case PESSIMISTIC_LOCKING -> moneyTransferService.transferPessimistic(requestId, newTransferDto);
            case SERIALIZABLE_ISOLATION -> moneyTransferService.transferSerializable(requestId, newTransferDto);
        };
    }

    /**
     * Persists the failed {@link Transaction}.
     *
     * @param requestId
     * @param newTransferDto
     * @param message
     * @throws ResourceNotFoundException
     */
    private void persistFailedTransaction(final UUID requestId, final NewTransferDto newTransferDto, final String message) throws ResourceNotFoundException {
        Account targetAccount = accountManagementService.getAccountByIdOrReturnDefault(newTransferDto.targetAccountId());
        Account sourceAccount = accountManagementService.getAccountByIdOrReturnDefault(newTransferDto.sourceAccountId());
        Transaction transaction = new Transaction(requestId, RequestStatus.FAILED, sourceAccount, targetAccount, newTransferDto.amount(), message, buildHashedPayloadFunction.apply(newTransferDto), sourceAccount.getCurrency());
        transactionRepository.save(transaction);
    }

    /**
     * Compares the current and saved hashed payload.
     *
     * @param transaction
     * @param newTransferDto
     * @throws RequestConflictException
     */
    private void validateIdempotent(final Transaction transaction, final NewTransferDto newTransferDto) throws RequestConflictException {
        if (buildHashedPayloadFunction.apply(newTransferDto) != transaction.getHashedPayload()) {
            var errorMessage = "The JSON body does not match with request requestId " + transaction.getId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }
}
