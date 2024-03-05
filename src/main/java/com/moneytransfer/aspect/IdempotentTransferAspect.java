package com.moneytransfer.aspect;

import com.moneytransfer.annotation.IdempotentTransferRequest;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Request;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.service.GetAccountService;
import com.moneytransfer.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    /**
     * The GetAccount service.
     */
    private final GetAccountService getAccountService;
    /**
     * The Request service.
     */
    private final RequestService requestService;
    /**
     * The transaction repository.
     */
    private final TransactionRepository transactionRepository;

    /**
     * Processes the idempotent transfer request.
     * @param proceedingJoinPoint
     * @param idempotentTransferRequest
     * @param requestId
     * @param newTransferDto
     * @return a (successful) Transaction
     * @throws Throwable
     */
    @Around("@annotation(idempotentTransferRequest) && execution(* transfer(java.util.UUID, com.moneytransfer.dto.NewTransferDto,..)) && args(requestId, newTransferDto,..)")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction handleIdempotentTransferRequest(ProceedingJoinPoint proceedingJoinPoint, IdempotentTransferRequest idempotentTransferRequest, UUID requestId, NewTransferDto newTransferDto) throws Throwable {
        return processRequest(requestId, newTransferDto, proceedingJoinPoint);
    }

    /**
     * Processes an idempotent transfer request.
     *
     * @param requestId
     * @param newTransferDto
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    private Transaction processRequest(final UUID requestId, final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            var request = getOrSubmitRequest(requestId, newTransferDto);
            validateIdempotent(request, newTransferDto);
            return switch (request.getRequestStatus()) {
                case SUBMITTED -> processTransfer(request, proceedingJoinPoint);
                case RESOLVED -> retrieveTransaction(request);
            };
        } catch (RuntimeException e) {
            getOrSubmitRequest(requestId, newTransferDto);
            throw e;
        }
    }

    /**
     * Gets or submits the request.
     *
     * @param requestId
     * @param newTransferDto
     * @return a submitted Request
     * @throws MoneyTransferException
     */
    private Request getOrSubmitRequest(final UUID requestId, final NewTransferDto newTransferDto) throws MoneyTransferException {
        Optional <Request> optionalRequest = Optional.ofNullable(requestService.getRequest(requestId));
        return optionalRequest.isPresent() ? optionalRequest.get() : requestService.submitRequest(requestId,newTransferDto);
    }

    /**
     * Processes the transfer and resolves the Request.
     *
     * @param request
     * @param proceedingJoinPoint
     * @return a new Transaction
     * @throws Throwable
     */
    private Transaction processTransfer(final Request request, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Transaction transaction = null;
        try {
            transaction = (Transaction) proceedingJoinPoint.proceed();
            return transaction;
        } catch (MoneyTransferException e) {
            transaction = persistFailedTransfer(request, e);
            throw e;
        } finally {
            requestService.resolveRequest(request.getRequestId(), transaction);
        }
    }

    /**
     * Retrieves the Transaction of a RESOLVED Request.
     *
     * @param request
     * @return the persisted Transaction
     * @throws Throwable
     */
    private Transaction retrieveTransaction(final Request request) throws Throwable {
        Transaction transaction = request.getTransaction();
        return switch (transaction.getTransactionStatus()) {
            case SUCCESSFUL_TRANSFER -> transaction;
            case FAILED_TRANSFER ->
                    throw new RequestConflictException(transaction.getMessage(), transaction.getHttpStatus());
        };
    }

    /**
     * Persists the failed transfer.
     *
     * @param request
     * @param e
     * @return a new failed Transaction
     * @throws ResourceNotFoundException
     */
    private Transaction persistFailedTransfer(final Request request, final MoneyTransferException e) throws ResourceNotFoundException {
        var targetAccount = getAccountService.getAccountByIdOrReturnDefault(request.getTargetAccountId());
        var sourceAccount = getAccountService.getAccountByIdOrReturnDefault(request.getSourceAccountId());
        var httpStatus = e.getHttpStatus();
        var errorMessage = e.getMessage();
        var currency = sourceAccount.getCurrency();
        return transactionRepository.save(new Transaction(UUID.randomUUID(), TransactionStatus.FAILED_TRANSFER, sourceAccount, targetAccount, request.getAmount(), errorMessage, currency, httpStatus));
    }


    /**
     * Validates payload idempotency.
     *
     * @param request
     * @param newTransferDto
     * @throws RequestConflictException
     */
    private void validateIdempotent(final Request request, final NewTransferDto newTransferDto) throws RequestConflictException {
        if (newTransferDto.hashCode() != request.toNewTransferDto().hashCode()) {
            var errorMessage = "The JSON body does not match with request id " + request.getRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

}
