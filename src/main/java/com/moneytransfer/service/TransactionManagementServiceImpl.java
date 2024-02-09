package com.moneytransfer.service;

import com.moneytransfer.component.BuildHashedPayloadFunction;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
     * Processes the request for a new {@link Transaction}.
     *
     * @param newTransferDto
     * @param requestId
     * @return Transaction
     * @throws MoneyTransferException
     */
    public Transaction processRequest(final NewTransferDto newTransferDto, final UUID requestId, final ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        RequestStatus status = getTransactionStatus(requestId);
        return switch (status) {
            case IN_PROGRESS -> processInProgressRequest(requestId, newTransferDto, concurrencyControlMode);
            case SUCCESS -> validateRequestAndGet(requestId, newTransferDto);
            case FAILED -> validateRequestAndThrow(requestId, newTransferDto);
        };
    }

    /**
     * Get associated {@link RequestStatus} if {@link Transaction} exists or return IN_PROGRESS.
     *
     * @param requestId
     * @return the status of the Transaction
     */
    private RequestStatus getTransactionStatus(final UUID requestId) {
        return transactionRepository.findById(requestId).map(Transaction::getStatus).orElse(RequestStatus.IN_PROGRESS);
    }

    /**
     * Process the {@link Transaction} in progress
     *
     * @param requestId
     * @param newTransferDto
     * @param concurrencyControlMode
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction processInProgressRequest(final UUID requestId, final NewTransferDto newTransferDto, final ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        try {
            return persistSuccessfulTransaction(requestId, concurrencyControlMode, newTransferDto);
        } catch (MoneyTransferException | RuntimeException e) {
            persistFailedTransaction(requestId, newTransferDto, getErrorMessage(e));
            throw e;
        }
    }

    /**
     * Gets the error message
     * if the Transaction has failed.
     *
     * @param e
     * @return errorMessage
     */
    private String getErrorMessage(Exception e) {
        String errorMessage = e.getMessage();
        if (e instanceof ConcurrencyFailureException) {
            errorMessage = "Another transaction has attempted to concurrently access the same account resources. Please try submitting a new request.";
        }
        return errorMessage;
    }

    /**
     * Validates idempotency and returns the associated {@link Transaction}
     *
     * @param requestId
     * @param newTransferDto
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction validateRequestAndGet(UUID requestId, NewTransferDto newTransferDto) throws MoneyTransferException {
        Transaction transaction = getTransactionById(requestId);
        validateIdempotent(transaction, newTransferDto);
        return transaction;
    }

    /**
     * Validates idempotency and throws a {@link RequestConflictException} with the appropriate error message.
     *
     * @param requestId
     * @param newTransferDto
     * @return
     * @throws MoneyTransferException
     */
    private Transaction validateRequestAndThrow(UUID requestId, NewTransferDto newTransferDto) throws MoneyTransferException {
        Transaction transaction = validateRequestAndGet(requestId, newTransferDto);
        throw new RequestConflictException(transaction.getMessage());
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

    /**
     * Persists the successful {@link Transaction}.
     *
     * @param requestId
     * @param concurrencyControlMode
     * @param newTransferDto
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction persistSuccessfulTransaction(final UUID requestId, final ConcurrencyControlMode concurrencyControlMode, final NewTransferDto newTransferDto) throws MoneyTransferException {
        return switch (concurrencyControlMode) {
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

}
