package service;

import com.moneytransactions.moneytransfer.dto.TransferAccountsDto;
import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.enums.Currency;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
        sourceAccount = new Account(1L, BigDecimal.ONE, Currency.EUR, LocalDateTime.now(), 0 );
    }

    @Test
    public void testHappyPath() {
        Account targetAccount = new Account(2L, BigDecimal.ZERO, Currency.EUR, LocalDateTime.now(), 0 );

        TransferAccountsDto transferAccountsDto = Mockito.mock(TransferAccountsDto.class);
        Mockito.when(transferAccountsDto.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(transferAccountsDto.getTargetAccount()).thenReturn(targetAccount);
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), targetAccount.getId()))
                .thenReturn(Optional.of(transferAccountsDto));

        Assertions.assertDoesNotThrow(() ->
                transactionServiceImpl.transferPessimistic(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ZERO, sourceAccount.getBalance());
        assertEquals(BigDecimal.ONE, targetAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.times(1)).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testInsufficientBalance() {
        Account targetAccount = new Account(2L, BigDecimal.ZERO, Currency.EUR, LocalDateTime.now(), 0 );

        TransferAccountsDto transferAccountsDto = Mockito.mock(TransferAccountsDto.class);
        Mockito.when(transferAccountsDto.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), targetAccount.getId()))
                .thenReturn(Optional.of(transferAccountsDto));

        assertThrows(InsufficientBalanceException.class, () ->
                transactionServiceImpl.transferPessimistic(sourceAccount.getId(), targetAccount.getId(), BigDecimal.TEN)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        assertEquals(BigDecimal.ZERO, targetAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testTransferSameAccount() {
        TransferAccountsDto transferAccountsDto = Mockito.mock(TransferAccountsDto.class);
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), sourceAccount.getId()))
                .thenReturn(Optional.of(transferAccountsDto));

        assertThrows(SameAccountException.class, () ->
                transactionServiceImpl.transferPessimistic(sourceAccount.getId(), sourceAccount.getId(), BigDecimal.ONE)
        );
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testAccountNotFound() {
        Long nonExistingAccountId = 2L;
        Mockito.when(accountRepository.findByIdAndLockPessimistic(sourceAccount.getId(), nonExistingAccountId))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionServiceImpl.transferPessimistic(sourceAccount.getId(), nonExistingAccountId, BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }


}
