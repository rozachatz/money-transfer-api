/**
  Test class for {@link com.moneytransfer.service.TransactionServiceImpl}
  This test uses embedded h2 db.
 */
package com.moneytransfer.service;

import com.moneytransfer.entity.Account;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
@RequiredArgsConstructor
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest

public class TransactionServiceImplTest {
    /**
     * Transaction Service
     */
    @Autowired
    private TransactionServiceImpl transactionService;
    /**
     * Account repository
     */
    @Autowired
   private AccountRepository accountRepository;
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
    public void setup(){
        BigDecimal balance = BigDecimal.valueOf(10);
        sourceAccount = new Account(0, UUID.randomUUID(), balance, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), balance, Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(targetAccount,sourceAccount));
    }

    /**
     * Happy path test
     * @throws MoneyTransferException
     */
    @Test
    public void testHappyPath() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), amount);
        BigDecimal expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal actualSourceBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        BigDecimal expectedTargetBalance = targetAccount.getBalance().add(amount);
        BigDecimal actualTargetBalance = retrievePersistedAccountBalance(targetAccount.getId());
        assertEquals(actualTargetBalance.stripTrailingZeros(),expectedTargetBalance.stripTrailingZeros());
    }

    /**
     * Insufficient source Account balance test
     * @throws MoneyTransferException
     */
    @Test(expected = InsufficientBalanceException.class)
    public void testInsufficientBalance() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance().multiply(BigDecimal.valueOf(10));
        transactionService.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), amount);
        BigDecimal actualSourceBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        BigDecimal expectedSourceBalance = sourceAccount.getBalance();
        assertEquals(actualSourceBalance,expectedSourceBalance);
        BigDecimal expectedTargetBalance = targetAccount.getBalance();
        BigDecimal actualTargetBalance = retrievePersistedAccountBalance(targetAccount.getId());
        assertEquals(actualTargetBalance,expectedTargetBalance);
    }

    /**
     * Same account transfer test
     * @throws MoneyTransferException
     */
    @Test(expected = SameAccountException.class)
    public void testTransferSameAccount() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.ONE;
        BigDecimal expectedBalance = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), sourceAccount.getId(), amount);
        BigDecimal actualBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        assertEquals(actualBalance,expectedBalance);
    }

    /**
     * Account not found test
     * @throws MoneyTransferException
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testAccountNotFound() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.ONE;
        UUID nonExistingAccountId = UUID.randomUUID();
        BigDecimal expectedBalance = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), nonExistingAccountId, amount);
        BigDecimal actualBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        assertEquals(actualBalance,expectedBalance);
    }

    private BigDecimal retrievePersistedAccountBalance(UUID accountId) throws ResourceNotFoundException {
        return transactionService.getAccountById(accountId).getBalance();
    }
}

