package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.*;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService { //responsible for business logic, error handling

    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction transferPessimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsPessimistic(sourceAccountId, targetAccountId);
        validateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);

        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();
        sourceAccount.debit(amount);
        targetAccount.credit(amount);
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));

        Transaction transaction = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, Currency.EUR);
        transactionRepository.save(transaction);
        return transaction;
    }

    @Transactional
    public Transaction transferOptimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsOptimistic(sourceAccountId, targetAccountId);
        validateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);

        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();
        sourceAccount.debit(amount);
        targetAccount.credit(amount);
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));

        Transaction transaction = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, Currency.EUR);
        transactionRepository.save(transaction);
        return transaction;
    }

    private void validateTransfer(TransferAccountsDto accounts, Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {

        if (sourceAccountId.equals(targetAccountId)) {  /* AC3: Same Account */
            String errorMessage = "Transfer in the same account is not allowed. ";
            logger.error(errorMessage + "Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId);
            throw new SameAccountException(errorMessage);
        }

        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            String errorMessage = "Insufficient balance in the source account. ";
            logger.error(errorMessage + " Account ID: " + sourceAccountId + ", Requested Amount: " + amount + ", Available Balance: " + balance);
            throw new InsufficientBalanceException(errorMessage);
        }

    }

    public TransferAccountsDto getAccountsByIdsPessimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .stream()
                .findAny()
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. ";
                    logger.error(errorMessage + "Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId);
                    return new AccountNotFoundException(errorMessage);
                });
    }

    public TransferAccountsDto getAccountsByIdsOptimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .stream()
                .findAny()
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. ";
                    logger.error(errorMessage + "Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId);
                    return new AccountNotFoundException(errorMessage);
                });
    }

    public Transaction getTransactionById(UUID id) throws TransactionNotFoundException {
        return transactionRepository.findById(id)
                .stream()
                .findAny()
                .orElseThrow(() -> {
                    String errorMessage = "Transaction not found.";
                    logger.error("Transaction with ID: " + id + "was not found.");
                    return new TransactionNotFoundException(errorMessage);
                });
    }

}