package service;

import com.moneytransactions.moneytransfer.dto.AccountsDTO;
import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.dto.TransferRequestDTO;
import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import com.moneytransactions.moneytransfer.service.TransactionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.DataProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MockitoSettings
public class TransactionServiceImplTest extends AbstractTransactionalTestNGSpringContextTests {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionServiceImpl transactionServiceImpl;
    private Account sourceAccount;

    @BeforeEach
    public void setup() {
        sourceAccount = new Account(BigDecimal.ONE, "EUR", LocalDateTime.now());
        sourceAccount.setId(1L);
    }
    @Test
    public void testHappyTransfer_Successful() {
        Account targetAccount = new Account(BigDecimal.ZERO, "EUR",LocalDateTime.now());
        targetAccount.setId(2L);

        AccountsDTO accountsDTO = Mockito.mock(AccountsDTO.class);
        Mockito.when(accountsDTO.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountsDTO.getTargetAccount()).thenReturn(targetAccount);
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), targetAccount.getId()))
                .thenReturn(Optional.of(accountsDTO));

        Assertions.assertDoesNotThrow(() ->
              transactionServiceImpl.transferFunds(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ONE,"Pessimistic")
        );
        assertEquals(BigDecimal.ZERO, sourceAccount.getBalance());
        assertEquals(BigDecimal.ONE, targetAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.times(1)).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testInsufficientBalance() {
        Account targetAccount = new Account(BigDecimal.ZERO, "EUR",LocalDateTime.now());
        targetAccount.setId(2L);

        AccountsDTO accountsDTO = Mockito.mock(AccountsDTO.class);
        Mockito.when(accountsDTO.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), targetAccount.getId()))
                .thenReturn(Optional.of(accountsDTO));

        assertThrows(InsufficientBalanceException.class, () ->
                transactionServiceImpl.transferFunds(sourceAccount.getId(), targetAccount.getId(), BigDecimal.TEN,"Pessimistic")
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        assertEquals(BigDecimal.ZERO, targetAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testTransferSameAccount() {
        AccountsDTO accountsDTO = Mockito.mock(AccountsDTO.class);
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), sourceAccount.getId()))
                .thenReturn(Optional.of(accountsDTO));

        assertThrows(SameAccountException.class, () ->
                transactionServiceImpl.transferFunds(sourceAccount.getId(), sourceAccount.getId(), BigDecimal.ONE,"Pessimistic")
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testAccountNotFound() {
        Long nonExistingAccountId = 2L;

        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), nonExistingAccountId))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionServiceImpl.transferFunds(sourceAccount.getId(), nonExistingAccountId, BigDecimal.ONE,"Pessimistic"));
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }


}
