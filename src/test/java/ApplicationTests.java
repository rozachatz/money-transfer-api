import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MockitoSettings
public class ApplicationTests {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionServiceImpl transactionServiceImpl;
    private Account sourceAccount;

    @BeforeEach
    public void setup() {
        sourceAccount = new Account(BigDecimal.ONE, "EUR");
        sourceAccount.setId(1L);
    }

    @Test
    public void testHappyTransfer_Successful() {
        Account targetAccount = new Account(BigDecimal.ZERO, "EUR");
        targetAccount.setId(2L); //Assign different id to target

        //Mock repo behavior
        Mockito.when(accountRepository.findAllByIdAndLock(Arrays.asList(sourceAccount.getId(), targetAccount.getId()))).thenReturn(Arrays.asList(sourceAccount, targetAccount));

        //Perform money transfer, no exceptions should be thrown
        Assertions.assertDoesNotThrow(() -> transactionServiceImpl.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ONE));

        //Verify balances: target credited and src debited
        assertEquals(BigDecimal.ZERO, sourceAccount.getBalance());
        assertEquals(BigDecimal.ONE, targetAccount.getBalance());

        //Transaction saved
        Mockito.verify(transactionRepository, Mockito.times(1)).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testInsufficientBalance() {
        Account targetAccount = new Account(BigDecimal.ZERO, "EUR");
        targetAccount.setId(2L); //Assign different id to target

        //Mock repo behavior
        Mockito.when(accountRepository.findAllByIdAndLock(Arrays.asList(sourceAccount.getId(), targetAccount.getId()))).thenReturn(Arrays.asList(sourceAccount, targetAccount));

        //Perform money transfer, insufficient balance exception should be thrown
        assertThrows(InsufficientBalanceException.class, () -> transactionServiceImpl.moneyTransfer(sourceAccount.getId(), targetAccount.getId(), BigDecimal.TEN));

        //Verify that balances remain unchanged
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());
        assertEquals(BigDecimal.ZERO, targetAccount.getBalance());

        //Verify that no transaction is saved
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testTransferSameAccount() {
        //Mock repo behavior
        Mockito.when(accountRepository.findAllByIdAndLock(Arrays.asList(sourceAccount.getId(), sourceAccount.getId()))).thenReturn(Arrays.asList(sourceAccount, sourceAccount));

        //Perform money transfer, same account exception should be thrown
        assertThrows(SameAccountException.class, () -> transactionServiceImpl.moneyTransfer(sourceAccount.getId(), sourceAccount.getId(), BigDecimal.ONE));

        //Verify the balance of source=target does not change
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());

        //Verify that no transaction is saved
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));
    }

    @Test
    public void testAccountNotFound() {
        //Assign non-existing id to target
        Long nonExistingAccountId = 2L;

        //Mock the behavior of the repository to return the source account
        Mockito.when(accountRepository.findAllByIdAndLock(Arrays.asList(sourceAccount.getId(), nonExistingAccountId))).thenReturn(Arrays.asList(sourceAccount));

        //Perform money transfer, non-existing account exception should be thrown
        assertThrows(AccountNotFoundException.class, () -> transactionServiceImpl.moneyTransfer(sourceAccount.getId(), nonExistingAccountId, BigDecimal.ONE));

        //Balance of existing account does not change
        assertEquals(BigDecimal.ONE, sourceAccount.getBalance());

        //Verify that no transaction is saved
        Mockito.verify(transactionRepository, Mockito.never()).save(ArgumentMatchers.any(Transaction.class));

    }


}
