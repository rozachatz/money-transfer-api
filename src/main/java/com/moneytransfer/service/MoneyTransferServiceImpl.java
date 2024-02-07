package com.moneytransfer.service;

import com.moneytransfer.component.BuildHashedPayloadFunction;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.RequestStatus;
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
     * Transfer with serializable isolation.
     *
     * @param newTransferDto
     * @param requestId
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transferSerializable(UUID requestId, NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = accountManagementService.getAccountsByIds(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return initiateTransfer(requestId, transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer with optimistic locking on the accounts.
     *
     * @param newTransferDto
     * @param requestId
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferOptimistic(UUID requestId, NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = accountManagementService.getAccountsByIdsOptimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return initiateTransfer(requestId, transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer with pessimistic locking on the accounts.
     *
     * @param newTransferDto
     * @param requestId
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    @Transactional
    public Transaction transferPessimistic(UUID requestId, NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = accountManagementService.getAccountsByIdsPessimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return initiateTransfer(requestId, transferAccountsDto, newTransferDto);
    }

    /**
     * Performs the transfer and currency exchange.
     *
     * @param transferAccountsDto
     * @param newTransferDto
     * @param requestId
     * @return new Transaction
     * @throws MoneyTransferException
     */
    private Transaction initiateTransfer(UUID requestId, TransferAccountsDto transferAccountsDto, NewTransferDto newTransferDto) throws MoneyTransferException {
        validateTransfer(transferAccountsDto, newTransferDto.amount());
        var sourceAccount = transferAccountsDto.getSourceAccount();
        var targetAccount = transferAccountsDto.getTargetAccount();
        transferAndExchange(sourceAccount, targetAccount, newTransferDto.amount());
        Transaction transaction = new Transaction(requestId, RequestStatus.SUCCESS, transferAccountsDto.getSourceAccount(), transferAccountsDto.getTargetAccount(), newTransferDto.amount(), "Transaction was executed successfully.", buildHashedPayloadFunction.apply(newTransferDto), transferAccountsDto.getSourceAccount().getCurrency());
        return transactionRepository.save(transaction);
    }

    /**
     * Transfers the amount in the source currency and exchanges to the target currency.
     *
     * @param sourceAccount
     * @param targetAccount
     * @param amount
     * @throws MoneyTransferException
     */
    private void transferAndExchange(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        sourceAccount.debit(amount);
        var targetAmount = exchangeSourceToTargetCurrency(sourceAccount, targetAccount, amount);
        targetAccount.credit(targetAmount);
    }

    /**
     * Exchanges the amount if needed.
     *
     * @param sourceAccount
     * @param targetAccount
     * @param amount
     * @return
     * @throws MoneyTransferException
     */
    private BigDecimal exchangeSourceToTargetCurrency(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        var sourceCurrency = sourceAccount.getCurrency();
        var targetCurrency = targetAccount.getCurrency();
        if (sourceCurrency != targetCurrency) {
            return currencyExchangeService.exchangeCurrency(amount, sourceCurrency, targetCurrency);
        }
        return amount;
    }

    /**
     * Validates transfer according to Acceptance Criteria.
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

}
