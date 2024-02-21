package com.moneytransfer.component;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.service.GetAccountService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    private final TransactionRepository transactionRepository;
    private final GetAccountService getAccountService;

    @Around("@annotation(idempotentTransferRequest) && execution(* transfer(java.util.UUID, com.moneytransfer.dto.NewTransferDto,..)) && args(transactionId, newTransferDto,..)")
    public Transaction handleIdempotentTransferRequest(ProceedingJoinPoint proceedingJoinPoint, IdempotentTransferRequest idempotentTransferRequest, UUID transactionId, NewTransferDto newTransferDto) throws Throwable {
        return processRequest(transactionId, newTransferDto, proceedingJoinPoint);
    }

    /**
     * Processes a new transfer request.
     *
     * @param transactionId
     * @param newTransferDto
     * @param proceedingJoinPoint
     * @return a successful Transaction
     * @throws Throwable
     */
    private Transaction processRequest(final UUID transactionId, final NewTransferDto newTransferDto, ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        var transactionStatus = getTransactionStatusById(transactionId);
        return switch (transactionStatus) {
            case SUCCESS -> validateRequestAndGet(transactionId, newTransferDto);
            case FAILED -> validateRequestAndThrow(transactionId, newTransferDto);
            case IN_PROGRESS -> processInProgressRequest(transactionId, newTransferDto, proceedingJoinPoint);
        };
    }

    private Transaction processInProgressRequest(final UUID transactionId, final NewTransferDto newTransferDto, ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            return (Transaction) proceedingJoinPoint.proceed();
        } catch (Exception e) {
            persistFailedTransaction(transactionId, newTransferDto, e);
            throw e;
        }
    }

    /**
     * Persists the failed {@link Transaction}.
     *
     * @param transactionId
     * @param newTransferDto
     * @param e
     * @throws ResourceNotFoundException
     */
    private void persistFailedTransaction(final UUID transactionId, final NewTransferDto newTransferDto, final Exception e) throws ResourceNotFoundException {
        var targetAccount = getAccountService.getAccountByIdOrReturnDefault(newTransferDto.targetAccountId());
        var sourceAccount = getAccountService.getAccountByIdOrReturnDefault(newTransferDto.sourceAccountId());
        var httpStatus = getFailedHttpStatus(e);
        var errorMessage = getErrorMessage(e);
        var currency = sourceAccount.getCurrency();
        transactionRepository.save(new Transaction(transactionId, TransactionStatus.FAILED, sourceAccount, targetAccount, newTransferDto.amount(), errorMessage, currency, httpStatus));
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

    /**
     * Gets the {@link TransactionStatus} by id.
     *
     * @param transactionId
     * @return the Transaction status
     */
    private TransactionStatus getTransactionStatusById(final UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(Transaction::getTransactionStatus).orElse(TransactionStatus.IN_PROGRESS);
    }

    /**
     * Validates idempotency and returns the associated {@link Transaction}.
     *
     * @param transactionId
     * @param newTransferDto
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction validateRequestAndGet(final UUID transactionId, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transaction = transactionRepository.findById(transactionId).get();
        validateIdempotent(transaction, newTransferDto);
        return transaction;
    }

    /**
     * Validates idempotency by comparing the hashed payloads.
     *
     * @param transaction
     * @param newTransferDto
     * @throws RequestConflictException
     */
    private void validateIdempotent(final Transaction transaction, final NewTransferDto newTransferDto) throws RequestConflictException {
        NewTransferDto persistedTransferDto = new NewTransferDto(transaction.getSourceAccount().getAccountId(), transaction.getTargetAccount().getAccountId(), transaction.getAmount());
        if (newTransferDto.hashCode() != persistedTransferDto.hashCode()) {
            var errorMessage = "The JSON body does not match with request id " + transaction.getTransactionId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    /**
     * Validates idempotency and throws a {@link RequestConflictException} with the appropriate error message.
     *
     * @param
     * @param newTransferDto
     * @throws MoneyTransferException
     */
    private Transaction validateRequestAndThrow(final UUID transactionId, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transaction = validateRequestAndGet(transactionId, newTransferDto);
        throw new RequestConflictException(transaction.getMessage(), transaction.getHttpStatus());
    }


}
