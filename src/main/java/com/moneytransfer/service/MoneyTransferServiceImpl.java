package com.moneytransfer.service;

import com.moneytransfer.component.IdempotentTransferRequest;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementation for {@link MoneyTransferService}.
 */

@Service
@RequiredArgsConstructor
class MoneyTransferServiceImpl implements MoneyTransferService {
    /**
     * The Currency exchange service.
     */
    private final CurrencyExchangeService currencyExchangeService;

    /**
     * The service that gets {@link Account} entities.
     */
    private final GetAccountService getAccountService;

    /**
     * The {@link Transaction} repository.
     */
    private final TransactionRepository transactionRepository;

    @IdempotentTransferRequest
    @Transactional
    public Transaction transfer(final UUID transactionId, final NewTransferDto newTransferDto, ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        return switch (concurrencyControlMode) {
            case SERIALIZABLE_ISOLATION -> transferSerializable(transactionId, newTransferDto);
            case OPTIMISTIC_LOCKING -> transferOptimistic(transactionId, newTransferDto);
            case PESSIMISTIC_LOCKING -> transferPessimistic(transactionId, newTransferDto);
        };
    }

    /**
     * Transfer money with serializable isolation.
     *
     * @param newTransferDto
     * @param transactionId
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    private Transaction transferSerializable(final UUID transactionId, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountsByIds(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transactionId, transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with optimistic locking.
     *
     * @param newTransferDto
     * @param transactionId
     * @return a new Transaction
     * @throws MoneyTransferException
     */

    private Transaction transferOptimistic(final UUID transactionId, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountsByIdsOptimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transactionId, transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with pessimistic locking.
     *
     * @param newTransferDto
     * @param transactionId
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction transferPessimistic(final UUID transactionId, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountsByIdsPessimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transactionId, transferAccountsDto, newTransferDto);
    }

    /**
     * Performs and validates the money transfer.
     *
     * @param transferAccountsDto
     * @param newTransferDto
     * @param transactionId
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction performTransfer(final UUID transactionId, final TransferAccountsDto transferAccountsDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, newTransferDto.amount());
        return persistTransaction(transactionId, transferAccountsDto, newTransferDto);
    }

    /**
     * Validates transfer according to ACs.
     *
     * @param accounts
     * @param amount
     * @throws MoneyTransferException
     */
    private void validateTransfer(final TransferAccountsDto accounts, final BigDecimal amount) throws MoneyTransferException {
        var sourceAccountId = accounts.getSourceAccount().getAccountId();
        var targetAccountId = accounts.getTargetAccount().getAccountId();
        if (sourceAccountId == targetAccountId) {
            var errorMessage = "Transfer in the same account is not allowed. Account transactionId: " + sourceAccountId + ".";
            throw new SameAccountException(errorMessage);
        }
        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {
            var errorMessage = "Insufficient balance in the source account. Account transactionId:  " + sourceAccountId + ", Requested Amount: " + amount + ", Available Balance: " + balance + ".";
            throw new InsufficientBalanceException(errorMessage);
        }
    }

    /**
     * Persists the successful {@link Transaction}.
     *
     * @param transactionId
     * @param transferAccountsDto
     * @param newTransferDto
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction persistTransaction(final UUID transactionId, final TransferAccountsDto transferAccountsDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var sourceAccount = transferAccountsDto.getSourceAccount();
        var targetAccount = transferAccountsDto.getTargetAccount();
        transferAndExchange(sourceAccount, targetAccount, newTransferDto.amount());
        var currency = sourceAccount.getCurrency();
        Transaction transaction = new Transaction(transactionId, TransactionStatus.SUCCESS, sourceAccount, targetAccount, newTransferDto.amount(), "Transaction was executed successfully.", currency, GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS);
        return transactionRepository.save(transaction);
    }

    /**
     * Transfers and exchanges the {@link Currency}.
     *
     * @param sourceAccount
     * @param targetAccount
     * @param amount
     * @throws MoneyTransferException
     */
    private void transferAndExchange(Account sourceAccount, Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        sourceAccount.debit(amount);
        var exchangedAmount = exchangeSourceCurrency(sourceAccount, targetAccount, amount);
        targetAccount.credit(exchangedAmount);
    }

    /**
     * Exchanges the source {@link Currency} to target if necessary.
     *
     * @param sourceAccount
     * @param targetAccount
     * @param amount
     * @return the exchanged amount
     * @throws MoneyTransferException
     */
    private BigDecimal exchangeSourceCurrency(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        var sourceCurrency = sourceAccount.getCurrency();
        var targetCurrency = targetAccount.getCurrency();
        if (sourceCurrency != targetCurrency) {
            return currencyExchangeService.exchangeCurrency(amount, sourceCurrency, targetCurrency);
        }
        return amount;
    }

}
