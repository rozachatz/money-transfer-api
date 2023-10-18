package com.moneytransfer.service;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
@Ignore
@SpringBootTest
public class TransactionServiceImplTest {
    @Autowired
    private TransactionServiceImpl transactionServiceImpl;
    @Autowired
    private AccountRepository accountRepository;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    public void setup() {
        List<Account> accounts = transactionServiceImpl.getAccountsWithLimit(2).getContent();
        sourceAccount = accounts.get(0);
        targetAccount = accounts.get(1);
    }

    @Test
    @Sql("classpath:data.sql")
    public void testHappyPath() {
        Assertions.assertDoesNotThrow(() ->
                transactionServiceImpl.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ZERO, sourceAccount.getBalance());
        assertEquals(BigDecimal.ONE, targetAccount.getBalance());
    }

    @Test
    public void testInsufficientBalance() {
        targetAccount.setBalance(BigDecimal.ZERO);
        assertThrows(InsufficientBalanceException.class, () ->
                transactionServiceImpl.transferOptimistic(sourceAccount.getId(), targetAccount.getId(), BigDecimal.TEN)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        assertEquals(BigDecimal.ZERO, targetAccount.getBalance());
    }

    @Test
    public void testTransferSameAccount() {
        assertThrows(SameAccountException.class, () ->
                transactionServiceImpl.transferOptimistic(sourceAccount.getId(), sourceAccount.getId(), BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
    }

    @Test
    public void testAccountNotFound() {
        UUID nonExistingAccountId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () ->
                transactionServiceImpl.transferOptimistic(sourceAccount.getId(), nonExistingAccountId, BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
    }


}
