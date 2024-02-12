package com.moneytransfer.service;

import com.moneytransfer.component.BuildHashedPayloadFunction;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
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
     * The Account Management service.
     */
    private final AccountManagementService accountManagementService;

    /**
     * The transaction repository
     */
    private final TransactionRepository transactionRepository;

    /**
     * The function that builds the hashed payload.
     */
    private final BuildHashedPayloadFunction buildHashedPayloadFunction;

    /**
     * Transfer money with serializable isolation.
     *
     * @param newTransferDto
     * @param id
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transferSerializable(final UUID id, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = accountManagementService.getAccountsByIds(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(id, transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with optimistic locking.
     *
     * @param newTransferDto
     * @param id
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferOptimistic(final UUID id, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = accountManagementService.getAccountsByIdsOptimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(id, transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with pessimistic locking.
     *
     * @param newTransferDto
     * @param id
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferPessimistic(final UUID id, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = accountManagementService.getAccountsByIdsPessimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(id, transferAccountsDto, newTransferDto);
    }

    /**
     * Performs and validates the money transfer.
     *
     * @param transferAccountsDto
     * @param newTransferDto
     * @param id
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction performTransfer(final UUID id, final TransferAccountsDto transferAccountsDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, newTransferDto.amount());
        return persistTransaction(id, transferAccountsDto, newTransferDto);
    }

    /**
     * Validates transfer according to ACs.
     *
     * @param accounts
     * @param amount
     * @throws MoneyTransferException
     */
    private void validateTransfer(final TransferAccountsDto accounts, final BigDecimal amount) throws MoneyTransferException {
        var sourceAccountId = accounts.getSourceAccount().getId();
        var targetAccountId = accounts.getTargetAccount().getId();
        if (sourceAccountId == targetAccountId) {
            var errorMessage = "Transfer in the same account is not allowed. Account ID: " + sourceAccountId + ".";
            throw new SameAccountException(errorMessage);
        }
        BigDecimal balance = accounts.getSourceAccount().getBalance();
        if (balance.compareTo(amount) < 0) {
            var errorMessage = "Insufficient balance in the source account. Account ID:  " + sourceAccountId + ", Requested Amount: " + amount + ", Available Balance: " + balance + ".";
            throw new InsufficientBalanceException(errorMessage);
        }
    }

    /**
     * Persists the successful {@link Transaction}.
     *
     * @param id
     * @param transferAccountsDto
     * @param newTransferDto
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction persistTransaction(final UUID id, final TransferAccountsDto transferAccountsDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var sourceAccount = transferAccountsDto.getSourceAccount();
        var targetAccount = transferAccountsDto.getTargetAccount();
        transferAndExchange(sourceAccount, targetAccount, newTransferDto.amount());
        var hashedPayload = buildHashedPayloadFunction.apply(newTransferDto);
        var currency = sourceAccount.getCurrency();
        Transaction transaction = new Transaction(id, TransactionStatus.SUCCESS, sourceAccount, targetAccount, newTransferDto.amount(), "Transaction was executed successfully.", hashedPayload, currency, GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS);
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
    private void transferAndExchange(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
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
