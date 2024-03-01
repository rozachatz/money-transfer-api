package com.moneytransfer.component;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.ResolvedRequestDto;
import com.moneytransfer.entity.Request;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.RequestRepository;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.service.GetAccountService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    private final GetAccountService getAccountService;
    private final RequestCacheManager requestCacheManager;
    private final TransactionRepository transactionRepository;

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
            var request = getOrSubmitRequest(requestId,newTransferDto);
            return switch (request.getRequestStatus()) {
                case SUBMITTED -> processTransfer(request, newTransferDto, proceedingJoinPoint);
                case RESOLVED -> retrieveTransaction(request, newTransferDto);
            };
        } catch (RuntimeException e) {
            getOrSubmitRequest(requestId,newTransferDto);
            throw e;
        }
    }
    private Request getOrSubmitRequest(final UUID requestId, final NewTransferDto newTransferDto){
        return Optional.ofNullable(requestCacheManager.getRequest(requestId)).orElseGet(()->submitRequest(requestId,newTransferDto));
    }

    private Request submitRequest(final UUID requestId, final NewTransferDto newTransferDto){
        return requestCacheManager.updateRequestCache(new Request(requestId, RequestStatus.SUBMITTED,newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId()));
    }
    /**
     * Processes the transfer and resolves the request.
     *
     * @param request
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    private Transaction processTransfer(final Request request, final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        validateSubmittedRequest(request, newTransferDto);
        Transaction transaction = null;
        try {
            transaction = (Transaction) proceedingJoinPoint.proceed();
            return transaction;
        } catch (MoneyTransferException e) {
            transaction = persistFailedTransfer(request, e);
            throw e;
        } finally {
            resolveRequest(request.getRequestId(), transaction);
        }
    }

    /**
     * Retrieves the Transaction of a resolved request.
     * @param request
     * @param newTransferDto
     * @return
     * @throws Throwable
     */
    private Transaction retrieveTransaction(final Request request, final NewTransferDto newTransferDto) throws Throwable {
        Transaction transaction = request.getTransaction();
        return switch (transaction.getTransactionStatus()) {
            case SUCCESSFUL_TRANSFER -> validateIdempotentAndGet(request, newTransferDto);
            case FAILED_TRANSFER -> validateIdempotentAndThrow(request, newTransferDto);
        };
    }

    /**
     * Idempotency and transaction validation for that an unresolved (submitted) request.
     * @param request
     * @param newTransferDto
     * @throws MoneyTransferException
     */
    private void validateSubmittedRequest(final Request request, final NewTransferDto newTransferDto) throws MoneyTransferException {
        if (Optional.ofNullable(request.getTransaction()).isPresent()) throw new RequestConflictException("Invalid submitted request: Unresolved requests should not contain an existing Transaction!");
        validateIdempotent(request, newTransferDto);
    }

    /**
     * Persists the failed transfer.
     *
     * @param request
     * @param e
     * @return
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
     * Resolves the request by associating it with a Transaction.
     * @param requestId
     * @param transaction
     */
    private void resolveRequest(final UUID requestId, final Transaction transaction) throws MoneyTransferException {
        validateTransactionExists(transaction);
        requestCacheManager.updateRequestCache(new Request(requestId,RequestStatus.RESOLVED,transaction));
    }

    /**
     * Transaction that resolves the request exists.
     * @param transaction
     * @return
     * @throws MoneyTransferException
     */
    private void validateTransactionExists(final Transaction transaction) throws MoneyTransferException {
        transactionRepository.findById(transaction.getTransactionId()).orElseThrow(()->new ResourceNotFoundException("Cannot resolve request: Associated Transaction was not found."));
    }

    /**
     * Validates idempotency and returns the associated {@link Transaction}.
     *
     * @param request
     * @param newTransferDto
     * @return the Transaction
     * @throws MoneyTransferException
     */
    private Transaction validateIdempotentAndGet(final Request request, final NewTransferDto newTransferDto) throws MoneyTransferException {
        validateIdempotent(request, newTransferDto);
        return request.getTransaction();
    }

    /**
     * Validates idempotency and throws a {@link RequestConflictException} with the appropriate error message.
     *
     * @param
     * @param newTransferDto
     * @throws MoneyTransferException
     */
    private Transaction validateIdempotentAndThrow(final Request request, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transaction = validateIdempotentAndGet(request, newTransferDto);
        throw new RequestConflictException(transaction.getMessage(), transaction.getHttpStatus());
    }

    /**
     * Validates idempotency by comparing the hashed payloads.
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
