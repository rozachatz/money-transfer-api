/**
 * Test class for {@link com.moneytransfer.service.MoneyTransferServiceImpl}
 * This test uses embedded h2 db.
 */
package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class MoneyTransferServiceImplTest {
    /**
     * Transaction Service
     */
    @Autowired
    private MoneyTransferServiceImpl transactionService;


    /**
     * Transaction Service
     */
    @Autowired
    private AccountManagementServiceImpl accountService;

    /**
     * Account repository
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * Transaction repository
     */
    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Source Account
     */
    private Account sourceAccount;
    /**
     * Target Account
     */
    private Account targetAccount;

    /**
     * Insert new accounts for each test
     */
    @Before
    public void setup() {
        BigDecimal balance = BigDecimal.valueOf(10);
        sourceAccount = new Account("Name1", 0, UUID.randomUUID(), balance, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account("Name2", 0, UUID.randomUUID(), balance, Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(targetAccount, sourceAccount));
    }

    /**
     * Happy path
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testHappyPath() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        var transactionId = UUID.randomUUID();
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccount.getBalance().add(amount);
        transactionService.transferSerializable(transactionId, new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount));
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getId());
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var actualTargetBalance = retrievePersistedBalance(targetAccount.getId());
        assertEquals(actualTargetBalance.stripTrailingZeros(), expectedTargetBalance.stripTrailingZeros());
        validateTransactionPersisted(transactionId);
    }

    /**
     * Insufficient balance
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testInsufficientBalance() throws MoneyTransferException {
        var amount = sourceAccount.getBalance().multiply(BigDecimal.valueOf(10));
        assertThrows(InsufficientBalanceException.class, () -> transactionService.transferSerializable(UUID.randomUUID(), new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount)));
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getId());
        var expectedSourceBalance = sourceAccount.getBalance();
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var expectedTargetBalance = targetAccount.getBalance();
        var actualTargetBalance = retrievePersistedBalance(targetAccount.getId()).stripTrailingZeros();
        assertEquals(actualTargetBalance.stripTrailingZeros(), expectedTargetBalance.stripTrailingZeros());
    }

    /**
     * Same account
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testTransferSameAccount() throws MoneyTransferException {
        var amount = BigDecimal.ONE;
        var expectedBalance = sourceAccount.getBalance();
        assertThrows(SameAccountException.class, () -> transactionService.transferSerializable(UUID.randomUUID(), new NewTransferDto(sourceAccount.getId(), sourceAccount.getId(), amount)));
        var actualBalance = retrievePersistedBalance(sourceAccount.getId());
        assertEquals(actualBalance.stripTrailingZeros(), expectedBalance.stripTrailingZeros());
    }

    /**
     * Account not found
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testAccountNotFound() throws MoneyTransferException {
        var amount = BigDecimal.ONE;
        var nonExistingAccountId = UUID.randomUUID();
        var expectedBalance = sourceAccount.getBalance();
        assertThrows(ResourceNotFoundException.class, () -> transactionService.transferSerializable(UUID.randomUUID(), new NewTransferDto(sourceAccount.getId(), nonExistingAccountId, amount)));
        var actualBalance = retrievePersistedBalance(sourceAccount.getId());
        assertEquals(actualBalance.stripTrailingZeros(), expectedBalance.stripTrailingZeros());
    }

    private BigDecimal retrievePersistedBalance(UUID accountId) throws ResourceNotFoundException {
        return accountService.getAccountById(accountId).getBalance();
    }

    private void validateTransactionPersisted(UUID transactionId) {
        Optional<Transaction> retrievedTransaction = transactionRepository.findById(transactionId);
        assertTrue(retrievedTransaction.isPresent());
        Assertions.assertEquals(retrievedTransaction.get().getTransactionStatus(), TransactionStatus.SUCCESS);
    }
}

