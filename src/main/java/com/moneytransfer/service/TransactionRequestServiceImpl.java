package com.moneytransfer.service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.repository.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementation for {@link TransactionRequestService}.
 */
@Service
@RequiredArgsConstructor
public class TransactionRequestServiceImpl implements TransactionRequestService {
    private final TransactionRequestRepository transactionRequestRepository;
    private final TransactionService transactionService;

    /** Processes the TransactionRequest.
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @param requestId
     * @return Transaction
     * @throws MoneyTransferException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction processRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID requestId) throws MoneyTransferException {
        TransactionRequest transactionRequest = getOrCreateTransactionRequest(requestId);
        String jsonBody = buildJsonString(sourceAccountId, targetAccountId, amount);
        return switch (transactionRequest.getRequestStatus()) {
            case SUCCESS -> {
                validateJson(transactionRequest, jsonBody);
                yield transactionRequest.getTransaction();
            }
            case IN_PROGRESS -> {
                transactionRequest.setJsonBody(jsonBody);
                yield processInProgressRequest(transactionRequest, sourceAccountId, targetAccountId, amount);
            }
            case FAILED -> {
                validateJson(transactionRequest, jsonBody);
                throw new RequestConflictException(transactionRequest.getErrorMessage());
            }
        };
    }

    private TransactionRequest getOrCreateTransactionRequest(UUID requestId) {
        return transactionRequestRepository.findById(requestId)
                .orElseGet(() -> transactionRequestRepository.save(new TransactionRequest(requestId, null, RequestStatus.IN_PROGRESS, "", "")));
    }

    private String buildJsonString(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
        return sourceAccountId.toString() + targetAccountId.toString() + amount.stripTrailingZeros();
    }

    private void validateJson(TransactionRequest transactionRequest, String jsonBody) throws RequestConflictException {
        if (!jsonBody.equals(transactionRequest.getJsonBody())) {
            String errorMessage = "The JSON body does not match with request ID " + transactionRequest.getRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    private Transaction processInProgressRequest(TransactionRequest transactionRequest, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        try {
            Transaction transaction = transactionService.transferSerializable(sourceAccountId, targetAccountId, amount);
            transactionRequest.setTransaction(transaction);
            transactionRequest.setRequestStatus(RequestStatus.SUCCESS);
            return transaction;
        } catch (MoneyTransferException | RuntimeException e) {
            transactionRequest.setErrorMessage(e.getMessage());
            transactionRequest.setRequestStatus(RequestStatus.FAILED);
            throw e;
        } finally {
            transactionRequestRepository.save(transactionRequest);
        }
    }


}
