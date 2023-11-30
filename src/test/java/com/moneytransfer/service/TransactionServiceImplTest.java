package com.moneytransfer.service;

import com.moneytransfer.entity.Account;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import org.checkerframework.checker.units.qual.A;
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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TransactionServiceImplTest {

    @Autowired
    private TransactionServiceImpl transactionService;
    @Autowired
    private AccountRepository accountRepository;
    private Account sourceAccount, targetAccount;
    @Before
    public void setup(){
        BigDecimal balance = BigDecimal.valueOf(10);
        sourceAccount = new Account(0, UUID.randomUUID(), balance, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), balance, Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(targetAccount,sourceAccount));
    }

    @Test
    public void testHappyPath() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), amount);
        BigDecimal expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal actualSourceBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        assert(actualSourceBalance.compareTo(expectedSourceBalance) == 0);
        BigDecimal expectedTargetBalance = targetAccount.getBalance().add(amount);
        BigDecimal actualTargetBalance = retrievePersistedAccountBalance(targetAccount.getId());
        assert(actualTargetBalance.compareTo(expectedTargetBalance) == 0);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testInsufficientBalance() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance().multiply(BigDecimal.valueOf(10));
        transactionService.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), amount);
        BigDecimal actualSourceBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        BigDecimal expectedSourceBalance = sourceAccount.getBalance();
        assert(actualSourceBalance.compareTo(expectedSourceBalance) == 0);
        BigDecimal expectedTargetBalance = targetAccount.getBalance();
        BigDecimal actualTargetBalance = retrievePersistedAccountBalance(targetAccount.getId());
        assert(actualTargetBalance.compareTo(expectedTargetBalance) == 0);
    }

    @Test(expected = SameAccountException.class)
    public void testTransferSameAccount() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.ONE;
        BigDecimal expectedBalance = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), sourceAccount.getId(), amount);
        BigDecimal actualBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        assert(actualBalance.compareTo(expectedBalance) == 0);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testAccountNotFound() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.ONE;
        UUID nonExistingAccountId = UUID.randomUUID();
        BigDecimal expectedBalance = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), nonExistingAccountId, amount);
        BigDecimal actualBalance = retrievePersistedAccountBalance(sourceAccount.getId());
        assert(actualBalance.compareTo(expectedBalance) == 0);
    }
    private BigDecimal retrievePersistedAccountBalance(UUID accountId) throws ResourceNotFoundException {
        return transactionService.getAccountById(accountId).getBalance();
    }
}

