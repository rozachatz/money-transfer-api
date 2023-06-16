package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.*;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
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
public class TransactionServiceImpl implements TransactionService { //responsible for business logic, error handling
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

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
            String errorMessage = "Transfer in the same account is not allowed. Account ID: "+sourceAccountId+"." ;
            throw new SameAccountException(errorMessage);
        }
        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            String errorMessage = "Insufficient balance in the source account. Account ID:  "+ sourceAccountId +  " ,Requested Amount: " + amount + ",Available Balance: " + balance+".";
            throw new InsufficientBalanceException(errorMessage);
        }
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIds(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);
    }
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
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId+".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
    public TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId+".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
    public TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLock(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId+".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
    public Transaction getById(UUID id) throws ResourceNotFoundException {
            return transactionRepository.findById(id)
                    .orElseThrow(() -> {
                        String errorMessage = "Transaction with ID: " + id + " was not found.";
                        return new ResourceNotFoundException(errorMessage);
                    });
    }
}