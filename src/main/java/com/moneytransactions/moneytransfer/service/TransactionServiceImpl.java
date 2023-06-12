package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.domain.TransferResult;
import com.moneytransactions.moneytransfer.dto.TransferAccountsDto;
import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.enums.Currency;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Struct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService { //responsible for business logic, error handling

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResult transferFundsPessimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsPessimistic(sourceAccountId, targetAccountId);

        validateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);

        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();

        sourceAccount.debit(amount);
        targetAccount.credit(amount);

        accountRepository.saveAll(List.of(sourceAccount, targetAccount));

        Transaction transaction = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, Currency.EUR);
        transactionRepository.save(transaction);
        return new TransferResult(transaction.getId(), sourceAccountId, targetAccountId, amount, LocalDateTime.now(), "Money transferred successfully.");

    }

    @Transactional
    public TransferResult transferFundsOptimistic(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsOptimistic(sourceAccountId, targetAccountId);

        validateTransfer(transferAccountsDto, sourceAccountId, targetAccountId, amount);

        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();

        sourceAccount.debit(amount);
        targetAccount.credit(amount);

        accountRepository.saveAll(List.of(sourceAccount, targetAccount));

        Transaction transaction = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, Currency.EUR);
        transactionRepository.save(transaction);
        return new TransferResult(transaction.getId(), sourceAccountId, targetAccountId, amount, LocalDateTime.now(), "Money transferred successfully.");

    }


    public TransferAccountsDto getAccountsByIdsPessimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .stream()
                .findAny()
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. ";
                    logger.error(errorMessage+"Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId);
                    return new AccountNotFoundException(errorMessage);
                });
    }

    public TransferAccountsDto getAccountsByIdsOptimistic(Long sourceAccountId, Long targetAccountId) throws AccountNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                                .stream()
                                .findAny()
                                .orElseThrow(() -> {
                                    String errorMessage = "Source/target account not found. ";
                                    logger.error(errorMessage+"Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId);
                                    return new AccountNotFoundException(errorMessage);
                                });
    }

    private void validateTransfer(TransferAccountsDto accounts, Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {

        if (sourceAccountId.equals(targetAccountId)) {  /* AC3: Same Account */
            String errorMessage = "Transfer in the same account is not allowed.. ";
            logger.error(errorMessage+"Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId);
            throw new SameAccountException(errorMessage);
        }
        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            String errorMessage = "Insufficient balance in the source account. ";
            logger.error(errorMessage+" Account ID: " + sourceAccountId + ", Requested Amount: " + amount + ", Available Balance: " + balance);
            throw new InsufficientBalanceException(errorMessage);
        }

    }

}