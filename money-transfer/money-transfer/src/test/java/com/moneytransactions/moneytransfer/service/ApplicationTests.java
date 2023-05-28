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
import org.junit.jupiter.api.Assertions;
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
    private BigDecimal initBalance;
    private Account sourceAccount;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        initBalance = new BigDecimal("100.00");
        //Create source account with initial balance, id = 1
        sourceAccount = new Account(initBalance,"EUR");
        sourceAccount.setId(1L);
    }

    @Test
    public void testHappyTransfer_Successful() {
        Account targetAccount = new Account(initBalance,"EUR");
        targetAccount.setId(2L); //assign different id to target

        //Mock repo behavior
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        BigDecimal amount = initBalance; //transaction amount <= src balance

        //Perform money transfer, no exceptions should be thrown
        Assertions.assertDoesNotThrow( () -> transactionService.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), amount));

        //Transaction saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));

        //Verify balances: target credited and src debited
        assertEquals(initBalance.subtract(amount), sourceAccount.getBalance());
        assertEquals(initBalance.add(amount), targetAccount.getBalance());
    }

    @Test
    public void testInsufficientBalance() {
        Account targetAccount = new Account(initBalance,"EUR");
        targetAccount.setId(2L); //Assign different id to target

        //Mock the behavior of the repositories
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        BigDecimal amount = initBalance.add(initBalance); //transaction amount > src balance

        //Perform money transfer, insufficient balance exception should be thrown
        assertThrows(InsufficientBalanceException.class, () -> transactionService.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), amount));

        //Verify that balances remain unchanged
        assertEquals(initBalance, sourceAccount.getBalance());
        assertEquals(initBalance, targetAccount.getBalance());

        //Verify that no transaction is saved
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testTransferSameAccount() {

        //Mock the behavior of the repositories
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));

        //Perform money transfer, same account exception should be thrown
        assertThrows(SameAccountException.class, () -> transactionService.moneyTransfer(sourceAccount.getId(), sourceAccount.getId(), initBalance));

        //Verify the balance of source/target does not change
        assertEquals(initBalance, sourceAccount.getBalance());

        //Verify that no transaction is saved
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testAccountNotFound() {
        //Assign non-existing id to target
        Long nonExistingAccountId = 2L;

        //Mock the behavior of the repository to return the source account
        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        // Mock the behavior of the repository to return an empty Optional for the target account
        when(accountRepository.findById(nonExistingAccountId)).thenReturn(Optional.empty());

        //Perform money transfer, non-existing account exception should be thrown
        assertThrows(AccountNotFoundException.class, () -> transactionService.moneyTransfer(sourceAccount.getId(), nonExistingAccountId, initBalance));

        //Balance of existing amount does not change
        assertEquals(initBalance, sourceAccount.getBalance());

        //Verify that no transaction is saved
        verify(transactionRepository, never()).save(any(Transaction.class));

    }


}

