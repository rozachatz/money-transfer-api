package com.moneytransfer.component;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.RequestUpdateDto;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    private final TransactionRepository transactionRepository;
    private final GetAccountService getAccountService;
    private final RequestRepository requestRepository;


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
            var request = getOrCreateRequest(requestId, newTransferDto);
            var requestStatus = request.getRequestStatus();
            return switch (requestStatus) {
                case SUBMITTED -> processTransfer(request, newTransferDto, proceedingJoinPoint);
                case RESOLVED -> retrieveTransaction(request, newTransferDto);
            };
        } catch (RuntimeException e) {
            requestRepository.save(createNewRequest(requestId, newTransferDto));
            throw e;
        }
    }

    /**
     * Gets or creates a new request.
     *
     * @param requestId
     * @param newTransferDto
     * @return
     */
    private Request getOrCreateRequest(final UUID requestId, final NewTransferDto newTransferDto) {
        return requestRepository.findById(requestId).orElse(createNewRequest(requestId, newTransferDto));
    }

    /**
     * Creates a new request.
     *
     * @param requestId
     * @param newTransferDto
     * @return
     */
    private Request createNewRequest(final UUID requestId, final NewTransferDto newTransferDto) {
        return new Request(requestId, RequestStatus.SUBMITTED, newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
    }

    /**
     * Processes the transfer and resolves the request.
     *
     * @param request
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    private Transaction processTransfer(final Request request, NewTransferDto newTransferDto, ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
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

    private void validateSubmittedRequest(Request request, NewTransferDto newTransferDto) throws MoneyTransferException {
        if (Optional.ofNullable(request.getTransaction()).isPresent())
            throw new MoneyTransferException("Invalid request: Submitted request should not contain an existing Transaction!");
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
     * Resolves the request.
     *
     * @param requestId
     * @param transaction
     */
    private void resolveRequest(final UUID requestId, final Transaction transaction) throws MoneyTransferException {
        var requestUpdateDto = validateAndGetRequestDto(requestId, transaction);
        requestRepository.findById(requestId)
                .ifPresentOrElse(existingRequest -> requestRepository.updateRequest(requestUpdateDto), () -> requestRepository.save(new Request(requestUpdateDto.getRequestId(), requestUpdateDto.getRequestStatus(), requestUpdateDto.getTransaction())));
    }

    private RequestUpdateDto validateAndGetRequestDto(UUID requestId, Transaction transaction) throws MoneyTransferException {
        Optional.ofNullable(transaction).orElseThrow(() -> new MoneyTransferException("Cannot resolve request: Associated Transaction was not found."));
        return new RequestUpdateDto(requestId, RequestStatus.RESOLVED, transaction);
    }


    /**
     * Retrieves the associated Transaction for a resolved request.
     *
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
