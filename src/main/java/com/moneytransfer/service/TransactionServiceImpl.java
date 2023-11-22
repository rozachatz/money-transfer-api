package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for {@link TransactionService}
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyExchangeService currencyExchangeService;

    /**
     * Transfer with optimistic locking
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferOptimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsOptimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, amount);
    }

    private TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Transfer with pessimistic locking
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferPessimistic(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) throws MoneyTransferException {
        TransferAccountsDto transferAccountsDto = getAccountsByIdsPessimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, amount);
    }

    private TransferAccountsDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Transfer with serializable isolation
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
        return initiateTransfer(transferAccountsDto, amount);
    }

    private TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Performs the transfer process and currency exchange (if needed).
     * @param transferAccountsDto
     * @param amount
     * @return
     * @throws MoneyTransferException
     */
    private Transaction initiateTransfer(TransferAccountsDto transferAccountsDto, BigDecimal amount) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, amount);
        Account sourceAccount = transferAccountsDto.getSourceAccount(), targetAccount = transferAccountsDto.getTargetAccount();
        performTransfer(sourceAccount, targetAccount, amount);
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));
        return transactionRepository.save(new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, targetAccount.getCurrency()));
    }

    private void performTransfer(Account sourceAccount, Account targetAccount, BigDecimal amount) throws MoneyTransferException {
        sourceAccount.debit(amount);
        BigDecimal targetAmount = calculateTargetAmount(sourceAccount,targetAccount,amount);
        targetAccount.credit(targetAmount);
    }

    private BigDecimal calculateTargetAmount(Account sourceAccount, Account targetAccount, BigDecimal amount) throws MoneyTransferException {
        Currency sourceCurrency=sourceAccount.getCurrency(), targetCurrency=targetAccount.getCurrency();
        if (sourceCurrency!=targetCurrency) return currencyExchangeService.exchangeCurrency(amount,sourceCurrency,targetCurrency);
        return amount;
    }

    private void validateTransfer(TransferAccountsDto accounts, BigDecimal amount) throws MoneyTransferException {
        UUID sourceAccountId = accounts.getSourceAccount().getId();
        UUID targetAccountId = accounts.getTargetAccount().getId();
        if (sourceAccountId == targetAccountId) {  /* AC3: Same Account */
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
     * Gets all accounts with limited number of results.
     *
     * @param limit
     * @return
     */
    public Page<Account> getAccountsWithLimit(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return accountRepository.findAll(pageRequest);
    }

    /**
     * Return all transactions within the given amount range
     *
     * @param minAmount
     * @param maxAmount
     * @return
     * @throws ResourceNotFoundException
     */
    public List<Transaction> getTransactionByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount)
                .orElseThrow(() -> {
                    String errorMessage = "Transactions within the specified range: [" + minAmount + "," + maxAmount + "] were not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
    public Transaction getTransactionById(UUID id) throws ResourceNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Transaction with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    public Account getAccountById(UUID id) throws ResourceNotFoundException {
        return accountRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Account with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
}