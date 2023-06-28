package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.*;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.repository.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionRequestRepository transactionRequestRepository;

    public TransactionRequest getOrCreateTransactionRequest(UUID requestId) {
        return transactionRequestRepository.findById(requestId)
                .orElseGet(() -> {
                    TransactionRequest transactionRequest = new TransactionRequest(requestId, RequestStatus.IN_PROGRESS);
                    return transactionRequestRepository.save(transactionRequest);
                });
    }

    //PROCESS REQUESTS
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction processRequest(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID requestId) throws MoneyTransferException {
        TransactionRequest transactionRequest = getOrCreateTransactionRequest(requestId);
        String JsonBody = sourceAccountId.toString() + targetAccountId.toString() + amount.toString();
        validateRequest(transactionRequest, JsonBody);
        switch (transactionRequest.getRequestStatus()) {
            case SUCCESS:
                return transactionRequest.getTransaction();
            case IN_PROGRESS:
                return processInProgressRequest(transactionRequest, sourceAccountId, targetAccountId, amount, JsonBody);
            case FAILED:
                throw new RequestConflictException(transactionRequest.getErrorMessage());
            default:
                String errorMessage = "Unexpected request status: " + transactionRequest.getRequestStatus();
                updateTransactionRequestOnFailure(transactionRequest, errorMessage);
                throw new MoneyTransferException(errorMessage);
        }
    }

    private Transaction processInProgressRequest(TransactionRequest transactionRequest, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String JsonBody) throws MoneyTransferException {
        try {
            Transaction transaction = transfer(sourceAccountId, targetAccountId, amount);
            updateTransactionRequestOnSuccess(transactionRequest, transaction);
            return transaction;
        } catch (MoneyTransferException | RuntimeException e) { //checked or unchecked (rollback)
            updateTransactionRequestOnFailure(transactionRequest, e.getMessage());
            throw e;//propagate
        } finally {
            transactionRequest.setRequestBodyJson(JsonBody);
            transactionRequestRepository.save(transactionRequest);
        }
    }

    private void validateRequest(TransactionRequest transactionRequest, String JsonBody) throws RequestConflictException {
        String requestJsonBody = transactionRequest.getRequestBodyJson();
        if (!JsonBody.equals(requestJsonBody) && requestJsonBody != null) {
            String errorMessage = "The JSON body does not match with request ID " + transactionRequest.getRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    private void updateTransactionRequestOnFailure(TransactionRequest transactionRequest, String errorMessage) {
        transactionRequest.setRequestStatus(RequestStatus.FAILED);
        transactionRequest.setErrorMessage(errorMessage);
    }

    private void updateTransactionRequestOnSuccess(TransactionRequest transactionRequest, Transaction transaction) {
        transactionRequest.setTransaction(transaction);
        transactionRequest.setRequestStatus(RequestStatus.SUCCESS);
    }

    //TRANSFER
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIds(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }

    public TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLock(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    private Transaction initiateTransfer(TransferAccountsDto transferAccountsDto, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();
        sourceAccount.debit(amount);
        targetAccount.credit(amount);
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));
        return transactionRepository.save(new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, targetAccount.getCurrency()));
    }

    private void validateTransfer(TransferAccountsDto accounts, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        if (sourceAccountId.equals(targetAccountId)) {  /* AC3: Same Account */
            String errorMessage = "Transfer in the same account is not allowed. Account ID: " + sourceAccountId + ".";
            throw new SameAccountException(errorMessage);
        }
        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            String errorMessage = "Insufficient balance in the source account. Account ID:  " + sourceAccountId + ", Requested Amount: " + amount + ", Available Balance: " + balance + ".";
            throw new InsufficientBalanceException(errorMessage);
        }
    }

    public Transaction getById(UUID id) throws ResourceNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Transaction with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    //TRANSFERS: PESSIMISTIC, OPTIMISTIC LOCKING
    @Transactional
    public Transaction transferPessimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsPessimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }

    @Transactional
    public Transaction transferOptimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsOptimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }

    public TransferAccountsDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    public TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }


}