package com.moneytransfer.service;

import com.moneytransfer.component.BuildHashedPayloadFunction;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
     * The money  transfer service.
     */
    private final MoneyTransferService moneyTransferService;
    /**
     * The account management service.
     */
    private final AccountManagementService accountManagementService;
    /**
     * The transaction repository.
     */
    private final TransactionRepository transactionRepository;
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
     * Gets {@link Transaction} by id.
     *
     * @param id
     * @return Transaction
     * @throws ResourceNotFoundException
     */
    public Transaction getTransactionById(final UUID id) throws ResourceNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    var errorMessage = "Transaction with id: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Processes the request for a {@link Transaction}.
     *
     * @param newTransferDto
     * @param id
     * @return Transaction
     * @throws MoneyTransferException
     */
    public Transaction processRequest(final UUID id, final NewTransferDto newTransferDto, final ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        var transactionStatus = getTransactionStatusById(id);
        return switch (transactionStatus) {
            case SUCCESS -> validateRequestAndGet(id, newTransferDto);
            case FAILED -> validateRequestAndThrow(id, newTransferDto);
            case IN_PROGRESS -> processInProgressRequest(id, newTransferDto, concurrencyControlMode);
        };
    }

    /**
     * Gets the associated {@link TransactionStatus} by id.
     *
     * @param id
     * @return the Transaction status
     */
    private TransactionStatus getTransactionStatusById(final UUID id) {
        return transactionRepository.findById(id)
                .map(Transaction::getTransactionStatus).orElse(TransactionStatus.IN_PROGRESS);
    }

    /**
     * Validates idempotency and returns the associated {@link Transaction}.
     *
     * @param id
     * @param newTransferDto
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction validateRequestAndGet(final UUID id, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transaction = getTransactionById(id);
        validateIdempotent(transaction, newTransferDto);
        return transaction;
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
            var errorMessage = "The JSON body does not match with request id " + transaction.getId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    /**
     * Validates idempotency and throws a {@link RequestConflictException} with the appropriate error message.
     *
     * @param id
     * @param newTransferDto
     * @throws MoneyTransferException
     */
    private Transaction validateRequestAndThrow(final UUID id, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transaction = validateRequestAndGet(id, newTransferDto);
        throw new RequestConflictException(transaction.getMessage(), transaction.getHttpStatus());
    }

    /**
     * Processes the {@link Transaction} in progress.
     *
     * @param id
     * @param newTransferDto
     * @param concurrencyControlMode
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction processInProgressRequest(final UUID id, final NewTransferDto newTransferDto, final ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        try {
            return persistSuccessfulTransaction(id, concurrencyControlMode, newTransferDto);
        } catch (MoneyTransferException | RuntimeException e) {
            persistFailedTransaction(id, newTransferDto, e);
            throw e;
        }
    }

    /**
     * Persists the successful {@link Transaction}.
     *
     * @param id
     * @param concurrencyControlMode
     * @param newTransferDto
     * @return the new successful Transaction
     * @throws MoneyTransferException
     */
    private Transaction persistSuccessfulTransaction(final UUID id, final ConcurrencyControlMode concurrencyControlMode, final NewTransferDto newTransferDto) throws MoneyTransferException {
        return switch (concurrencyControlMode) {
            case OPTIMISTIC_LOCKING -> moneyTransferService.transferOptimistic(id, newTransferDto);
            case PESSIMISTIC_LOCKING -> moneyTransferService.transferPessimistic(id, newTransferDto);
            case SERIALIZABLE_ISOLATION -> moneyTransferService.transferSerializable(id, newTransferDto);
        };
    }

    /**
     * Persists the failed {@link Transaction}.
     *
     * @param id
     * @param newTransferDto
     * @param e
     * @throws ResourceNotFoundException
     */
    private void persistFailedTransaction(final UUID id, final NewTransferDto newTransferDto, final Exception e) throws ResourceNotFoundException {
        var targetAccount = accountManagementService.getAccountByIdOrReturnDefault(newTransferDto.targetAccountId());
        var sourceAccount = accountManagementService.getAccountByIdOrReturnDefault(newTransferDto.sourceAccountId());
        var httpStatus = getFailedHttpStatus(e);
        var hashedPayload = buildHashedPayloadFunction.apply(newTransferDto);
        var errorMessage = getErrorMessage(e);
        var currency = sourceAccount.getCurrency();
        transactionRepository.save(new Transaction(id, TransactionStatus.FAILED, sourceAccount, targetAccount, newTransferDto.amount(), errorMessage, hashedPayload, currency, httpStatus));
    }

    /**
     * Gets the {@link HttpStatus} for the failed {@link Transaction}.
     *
     * @param e
     * @return the generic or specific http_status
     */
    private HttpStatus getFailedHttpStatus(Exception e) {
        if (e instanceof MoneyTransferException) {
            return ((MoneyTransferException) e).getHttpStatus();
        }
        return GlobalAPIExceptionHandler.GENERIC_ERROR_HTTP_STATUS;
    }

    /**
     * Gets the error message for the failed {@link Transaction}.
     *
     * @param e
     * @return the error message
     */
    private String getErrorMessage(final Exception e) {
        if (e instanceof ConcurrencyFailureException) {
            return "Concurrent modification error: Another transaction in progress has attempted to concurrently modify the same resources. Please try re-submitting a new request.";
        }
        return e.getMessage();
    }

}
