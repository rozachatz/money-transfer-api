package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MockitoSettings
public class TransactionServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionServiceImpl transactionServiceImpl;
    private Account sourceAccount;

    @BeforeEach
    public void setup() {
        sourceAccount = new Account(0, UUID.randomUUID(), BigDecimal.ONE, Currency.EUR, LocalDateTime.now());
    }

    @Test
    public void testHappyPath() {
        Account targetAccount = new Account(0, UUID.randomUUID(), BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());

        TransferAccountsDto transferAccountsDto = Mockito.mock(TransferAccountsDto.class);
        Mockito.when(transferAccountsDto.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(transferAccountsDto.getTargetAccount()).thenReturn(targetAccount);
        Mockito.when(accountRepository.findByIdAndLock(sourceAccount.getId(), targetAccount.getId()))
                .thenReturn(Optional.of(transferAccountsDto));

        Assertions.assertDoesNotThrow(() ->
                transactionServiceImpl.transfer(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ZERO, sourceAccount.getBalance());
        assertEquals(BigDecimal.ONE, targetAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.times(1)).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testInsufficientBalance() {
        Account targetAccount = new Account(0, UUID.randomUUID(), BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());

        TransferAccountsDto transferAccountsDto = Mockito.mock(TransferAccountsDto.class);
        Mockito.when(transferAccountsDto.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountRepository.findByIdAndLock(sourceAccount.getId(), targetAccount.getId()))
                .thenReturn(Optional.of(transferAccountsDto));

        assertThrows(InsufficientBalanceException.class, () ->
                transactionServiceImpl.transfer(sourceAccount.getId(), targetAccount.getId(), BigDecimal.TEN)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        assertEquals(BigDecimal.ZERO, targetAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testTransferSameAccount() {
        TransferAccountsDto transferAccountsDto = Mockito.mock(TransferAccountsDto.class);
        Mockito.when(accountRepository.findByIdAndLock(sourceAccount.getId(), sourceAccount.getId()))
                .thenReturn(Optional.of(transferAccountsDto));

        assertThrows(SameAccountException.class, () ->
                transactionServiceImpl.transfer(sourceAccount.getId(), sourceAccount.getId(), BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testAccountNotFound() {
        UUID nonExistingAccountId = UUID.randomUUID();
        Mockito.when(accountRepository.findByIdAndLock(sourceAccount.getId(), nonExistingAccountId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionServiceImpl.transfer(sourceAccount.getId(), nonExistingAccountId, BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }


}
