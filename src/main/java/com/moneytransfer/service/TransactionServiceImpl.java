package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
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
 * Implementation for {@link TransactionService}.
 */

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    /**
     * The Account repository.
     */
    private final AccountRepository accountRepository;
    /**
     * The Transaction repository.
     */
    private final TransactionRepository transactionRepository;
    /**
     * The Currency exchange service.
     */
    private final CurrencyExchangeService currencyExchangeService;

    /**
     * Transfer with optimistic locking.
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @param amount
     * @return Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferOptimistic(
            final UUID sourceAccountId, final UUID targetAccountId, final BigDecimal amount) throws MoneyTransferException {
        var transferAccountsDto = getAccountsByIdsOptimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, amount);
    }

    private TransferAccountsDto getAccountsByIdsOptimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    var errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
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
    public Transaction transferPessimistic(final UUID sourceAccountId, final UUID targetAccountId, final BigDecimal amount) throws MoneyTransferException {
        var transferAccountsDto = getAccountsByIdsPessimistic(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, amount);
    }

    private TransferAccountsDto getAccountsByIdsPessimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    var errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
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
    public Transaction transferSerializable(final UUID sourceAccountId, final UUID targetAccountId, final BigDecimal amount) throws MoneyTransferException {
        var transferAccountsDto = getAccountsByIds(sourceAccountId, targetAccountId);
        return initiateTransfer(transferAccountsDto, amount);
    }

    private TransferAccountsDto getAccountsByIds(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Performs the transfer process and currency exchange (if needed).
     *
     * @param transferAccountsDto
     * @param amount
     * @return new Transaction
     * @throws MoneyTransferException
     */
    private Transaction initiateTransfer(final TransferAccountsDto transferAccountsDto, final BigDecimal amount) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, amount);
        var sourceAccount = transferAccountsDto.getSourceAccount();
        var targetAccount = transferAccountsDto.getTargetAccount();
        performTransfer(sourceAccount, targetAccount, amount);
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));
        return transactionRepository.save(new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, targetAccount.getCurrency()));
    }

    private void performTransfer(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        sourceAccount.debit(amount);
        var targetAmount = calculateTargetAmount(sourceAccount, targetAccount, amount);
        targetAccount.credit(targetAmount);
    }

    private BigDecimal calculateTargetAmount(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        var sourceCurrency = sourceAccount.getCurrency();
        var targetCurrency = targetAccount.getCurrency();
        if (sourceCurrency != targetCurrency) {
            return currencyExchangeService.exchangeCurrency(amount, sourceCurrency, targetCurrency);
        }
        return amount;
    }

    private void validateTransfer(final TransferAccountsDto accounts, final BigDecimal amount) throws MoneyTransferException {
        var sourceAccountId = accounts.getSourceAccount().getId();
        var targetAccountId = accounts.getTargetAccount().getId();
        if (sourceAccountId == targetAccountId) {  /* AC3: Same Account */
            var errorMessage = "Transfer in the same account is not allowed. Account ID: " + sourceAccountId + ".";
            throw new SameAccountException(errorMessage);
        }
        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            var errorMessage = "Insufficient balance in the source account. Account ID:  " + sourceAccountId + ", Requested Amount: " + amount + ", Available Balance: " + balance + ".";
            throw new InsufficientBalanceException(errorMessage);
        }
    }

    /**
     * Gets all accounts with limited number of results.
     *
     * @param limit
     * @return Accounts
     */
    public Page<Account> getAccountsWithLimit(final int limit) {
        var pageRequest = PageRequest.of(0, limit);
        return accountRepository.findAll(pageRequest);
    }

    /**
     * Return all transactions within the given amount range.
     *
     * @param minAmount
     * @param maxAmount
     * @return Transactions
     * @throws ResourceNotFoundException
     */
    public List<Transaction> getTransactionByAmountBetween(final BigDecimal minAmount, final BigDecimal maxAmount) throws ResourceNotFoundException {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount)
                .orElseThrow(() -> {
                    var errorMessage = "Transactions within the specified range: [" + minAmount + "," + maxAmount + "] were not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Get Transaction by id.
     *
     * @param id
     * @return Transaction
     * @throws ResourceNotFoundException
     */

    public Transaction getTransactionById(final UUID id) throws ResourceNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    var errorMessage = "Transaction with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Get Account by id.
     *
     * @param id
     * @return Transaction
     * @throws ResourceNotFoundException
     */
    public Account getAccountById(final UUID id) throws ResourceNotFoundException {
        return accountRepository.findById(id)
                .orElseThrow(() -> {
                    var errorMessage = "Account with ID: " + id + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
}
