package com.moneytransfer.service;

import com.moneytransfer.MoneyTransferApplication;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MoneyTransferApplication.class)
public class TransactionServiceImplTest {

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private AccountRepository accountRepository;
    private Account sourceAccount, targetAccount;

    @Before
    public void setup() throws ResourceNotFoundException {
        sourceAccount = transactionService.getAccountById(UUID.fromString("6a7d71f0-6f12-45a6-91a1-198272a09fe8"));
        targetAccount = transactionService.getAccountById(UUID.fromString("e4c6f84c-8f92-4f2b-90bb-4352e9379bca"));
    }

    @Test
    public void testHappyPath() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.valueOf(1);
        BigDecimal expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal expectedTargetBalance = targetAccount.getBalance().add(amount);
        transactionService.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), amount);
        assertAccountBalance(sourceAccount, expectedSourceBalance);
        assertAccountBalance(targetAccount, expectedTargetBalance);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testInsufficientBalance() throws MoneyTransferException {
        BigDecimal expectedSourceBalance = sourceAccount.getBalance();
        BigDecimal expectedTargetBalance = targetAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), BigDecimal.valueOf(100));
        assertAccountBalance(sourceAccount, expectedSourceBalance);
        assertAccountBalance(targetAccount, expectedTargetBalance);
    }

    @Test(expected = SameAccountException.class)
    public void testTransferSameAccount() throws MoneyTransferException {
        BigDecimal expectedBalance = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), sourceAccount.getId(), BigDecimal.ONE);
        assertAccountBalance(sourceAccount, expectedBalance);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testAccountNotFound() throws MoneyTransferException {
        UUID nonExistingAccountId = UUID.randomUUID();
        BigDecimal expectedBalance = sourceAccount.getBalance();
        transactionService.transferOptimistic(sourceAccount.getId(), nonExistingAccountId, BigDecimal.ONE);
        assertAccountBalance(sourceAccount, expectedBalance);
    }

    private void assertAccountBalance(Account account, BigDecimal expectedBalance) {
        assertEquals(expectedBalance, accountRepository.findById((account.getId()))
                .map(Account::getBalance)
                .orElse(BigDecimal.valueOf(-1))); //balance can never be negative
    }
}
