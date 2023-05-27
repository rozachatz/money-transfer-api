package com.moneytransactions.moneytransfer.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApplicationTests {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testHappyTransfer_Successful() {

        // Create two test accounts with initial balances
        Account sourceAccount = new Account(new BigDecimal("100.00"),"EUR");
        sourceAccount.setId(1L); // Assign an ID to the source account

        Account targetAccount = new Account(new BigDecimal("50.00"),"EUR");
        targetAccount.setId(2L); // Assign an ID to the source account

        // Mock the behavior of the repositories
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        // Perform the money transfer
        transactionService.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), new BigDecimal("30.00"));

        // Verify the balances after the transfer
        assertEquals(new BigDecimal("70.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("80.00"), targetAccount.getBalance());

        // Verify that one transaction is saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testInsufficientBalance() {
        // Create two test accounts with initial balances
        Account sourceAccount = new Account(new BigDecimal("100.00"),"EUR");
        sourceAccount.setId(1L); // Assign an ID to the source account

        Account targetAccount = new Account(new BigDecimal("50.00"),"EUR");
        targetAccount.setId(2L); // Assign an ID to the source account

        // Mock the behavior of the repositories
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), new BigDecimal("120.00")); // amount > balance
        });  //second argument: code block that is expected to throw the exception
            // lambda usage: pass function as argument

        //Verify the balances of source & target do not change
        assertEquals(new BigDecimal("100.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("50.00"), targetAccount.getBalance());

    }
    @Test
    public void testTransferSameAccount() {
        // Create two test accounts with initial balances
        Account sourceAccount = new Account(new BigDecimal("100.00"),"EUR");
        sourceAccount.setId(1L); // Assign an ID to the source account

        Account targetAccount = sourceAccount;

        // Mock the behavior of the repositories
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        assertThrows(SameAccountException.class, () -> {
            transactionService.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), new BigDecimal("100.00"));
        });

        // Verify the balance of source/target does not change
        assertEquals(new BigDecimal("100.00"), sourceAccount.getBalance());

    }

    @Test
    public void testAccountNotFound() {
        // Create two test accounts with initial balances
        Account sourceAccount = new Account(new BigDecimal("100.00"),"EUR");
        sourceAccount.setId(1L); // Assign an ID to the source account
        Long nonExistingAccountId = 2L; // ID of the non-existing target account

        // Mock the behavior of the repository to return the source account
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        // Mock the behavior of the repository to return an empty Optional for the target account
        when(accountRepository.findById(nonExistingAccountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> {
            transactionService.moneyTransfer(sourceAccount.getId(), nonExistingAccountId, new BigDecimal("100.00"));
        });

        // Balance of existing amount does not change
        assertEquals(new BigDecimal("100.00"), sourceAccount.getBalance());

    }




}

