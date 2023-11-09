package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.*;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.repository.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation for {@link TransactionService}
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Gets all the bank accounts
     * @return all accounts with limit
     */
    public Page<Account> getAccountsWithLimit(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return accountRepository.findAll(pageRequest);
    }
    /**
     *
     * @param minAmount
     * @param maxAmount
     * @return A list of Transactions with amount in the given range
     * @throws ResourceNotFoundException
     */
    public List<Transaction> getTransactionByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount)
                .orElseThrow(() -> {
                    String errorMessage = "Transactions within the specified range: [" +minAmount+","+maxAmount + "] were not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     *
     * @param id
     * @return Transaction with the given id
     * @throws ResourceNotFoundException
     */
    public Transaction getTransactionById(UUID id) throws ResourceNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Transaction with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     *
     * @param id
     * @return Account with the given id
     * @throws ResourceNotFoundException
     */
    public Account getAccountById(UUID id) throws ResourceNotFoundException {
        return accountRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Account with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }


    /**
     *
     * @param transferAccountsDto
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction initiateTransfer(TransferAccountsDto transferAccountsDto, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();
        sourceAccount.debit(amount);
        targetAccount.credit(amount);
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));
        return transactionRepository.save(new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, targetAccount.getCurrency()));
    }

    /**
     * Validates the transfer operation of a new Transaction
     * @param accounts
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @throws MoneyTransferException
     */
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

    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return the associated Accounts (projection as a dto)
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }


    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return TransferAccountsDto
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }


    /**
     * New Transaction with optimistic locking for Accounts
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferOptimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsOptimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }

    /**
     * New Transaction with pessimistic locking for Accounts
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferPessimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsPessimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }
    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transferSerializable(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIds(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }
    /**
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return TransferAccountsDto
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
}