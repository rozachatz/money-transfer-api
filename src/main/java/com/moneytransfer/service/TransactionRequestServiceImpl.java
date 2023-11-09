package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.repository.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionRequestServiceImpl implements TransactionRequestService{
    private final TransactionRequestRepository transactionRequestRepository;
    private final TransactionService transactionService;
    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @param requestId
     * @return the Transaction associated with the TransactionRequest
     * @throws MoneyTransferException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction processRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID requestId) throws MoneyTransferException {
        TransactionRequest transactionRequest = getOrCreateTransactionRequest(requestId);
        String JsonBody = buildJsonString(sourceAccountId, targetAccountId, amount);
        return switch (transactionRequest.getRequestStatus()) {
            case SUCCESS -> {
                validateJson(transactionRequest, JsonBody);
                yield transactionRequest.getTransaction();
            }
            case IN_PROGRESS ->
                    processInProgressRequest(transactionRequest, sourceAccountId, targetAccountId, amount, JsonBody);
            case FAILED -> {
                validateJson(transactionRequest, JsonBody);
                throw new RequestConflictException(transactionRequest.getErrorMessage());
            }
        };
    }
    /**
     * Gets or creates a Transaction request
     * @return the Transaction request
     */
    private TransactionRequest getOrCreateTransactionRequest(UUID requestId) {
        return transactionRequestRepository.findById(requestId)
                .orElseGet(() -> transactionRequestRepository.save(new TransactionRequest(requestId, RequestStatus.IN_PROGRESS)));
    }

    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return String to compare with json body of Transaction request
     */
    private String buildJsonString(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
        return sourceAccountId.toString() + targetAccountId.toString() + amount.stripTrailingZeros();
    }
    /**
     * Validates if the json body of the TransactionRequest matches with the stored JsonBody
     * @param transactionRequest
     * @param JsonBody
     * @throws RequestConflictException
     */
    private void validateJson(TransactionRequest transactionRequest, String JsonBody) throws RequestConflictException {
        if (!JsonBody.equals(transactionRequest.getJsonBody())) {
            String errorMessage = "The JSON body does not match with request ID " + transactionRequest.getRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    /**
     *
     * @param transactionRequest
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @param JsonBody
     * @return a new Transaction associated with the TransactionRequest
     * @throws MoneyTransferException
     */
    private Transaction processInProgressRequest(TransactionRequest transactionRequest, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String JsonBody) throws MoneyTransferException {
        try {
            Transaction transaction = transfer(sourceAccountId, targetAccountId, amount);
            updateTransactionRequestOnSuccess(transactionRequest, transaction);
            return transaction;
        } catch (MoneyTransferException | RuntimeException e) { //checked or unchecked (rollback)
            updateTransactionRequestOnFailure(transactionRequest, e.getMessage());
            throw e;
        } finally {
            transactionRequest.setJsonBody(JsonBody);
            transactionRequestRepository.save(transactionRequest);
        }
    }

    /**
     *
     * @param transactionRequest
     * @param errorMessage
     */
    private void updateTransactionRequestOnFailure(TransactionRequest transactionRequest, String errorMessage) {
        transactionRequest.setRequestStatus(RequestStatus.FAILED);
        transactionRequest.setErrorMessage(errorMessage);
    }

    /**
     *
     * @param transactionRequest
     * @param transaction
     */
    private void updateTransactionRequestOnSuccess(TransactionRequest transactionRequest, Transaction transaction) {
        transactionRequest.setRequestStatus(RequestStatus.SUCCESS);
        transactionRequest.setTransaction(transaction);
    }

    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = transactionService.getAccountsByIds(sourceAccountId, targetAccountId);
        return transactionService.initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }
}
