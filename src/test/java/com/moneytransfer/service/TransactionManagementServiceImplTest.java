/**
 * Test class for {@link com.moneytransfer.service.TransactionManagementService}
 * This test uses embedded h2 db.
 */
package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionStatus;
import com.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TransactionManagementServiceImplTest {
    /**
     * The source and target accounts used in tests
     */
    Account sourceAccount, targetAccount;
    /**
     * The transactionManagementRequest service
     */
    @Autowired
    private TransactionManagementServiceImpl transactionManagementService;
    /**
     * The transaction repository
     */
    @Autowired
    private TransactionRepository transactionRepository;
    /**
     * The account repository
     */
    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void setup() {
        sourceAccount = new Account("Name1", 0, UUID.randomUUID(), BigDecimal.valueOf(10), Currency.EUR, LocalDateTime.now());
        targetAccount = new Account("Name2", 0, UUID.randomUUID(), BigDecimal.valueOf(10), Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(targetAccount, sourceAccount));
    }

    /**
     * Validates that a failed transfer request is persisted.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testPersisted_FailedRequest() {
        var amount = sourceAccount.getBalance();
        var nonExistentAccountId = UUID.randomUUID();
        var id = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(sourceAccount.getId(), nonExistentAccountId, amount);
        assertThrows(MoneyTransferException.class, () -> transactionManagementService.processRequest(id, newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        Optional<Transaction> retrievedTransaction = transactionRepository.findById(id);
        assertTrue(retrievedTransaction.isPresent());
        assertEquals(retrievedTransaction.get().getTransactionStatus(), TransactionStatus.FAILED);
    }

    /**
     * Validates that a successful transfer request is persisted.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testPersisted_SuccessfulRequest() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        var id = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount);
        var transaction = transactionManagementService.processRequest(id, newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        assertEquals(transaction.getHttpStatus(), GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS);
        assertEquals(transaction.getTransactionStatus(), TransactionStatus.SUCCESS);
    }

    /**
     * Checks the idempotent behavior of a transfer request.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testIdempotent_SuccessfulRequest() throws MoneyTransferException {
        var id = UUID.randomUUID();
        var amount = sourceAccount.getBalance();
        var newTransferDto = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount);
        var transaction1 = transactionManagementService.processRequest(id, newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        var transaction2 = transactionManagementService.processRequest(id, newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
        assertEquals(transaction1.getHttpStatus(), GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS);
        assertEquals(transaction1.getTransactionStatus(), TransactionStatus.SUCCESS);
    }

    /**
     * Checks the idempotent behavior of a failed transfer request.
     */
    @Test
    public void testIdempotent_FailedRequest() {
        var id = UUID.randomUUID();
        var amount = sourceAccount.getBalance().multiply(BigDecimal.TEN);
        var newTransferDto = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount);
        var exception1 = assertThrows(MoneyTransferException.class, () -> transactionManagementService.processRequest(id, newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        var exception2 = assertThrows(MoneyTransferException.class, () -> transactionManagementService.processRequest(id, newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        assertEquals(exception2.getMessage(), exception1.getMessage());
        assertEquals(exception2.getHttpStatus(), exception1.getHttpStatus());
    }

    /**
     * Checks the idempotent behavior of a transfer request regarding the payload.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testIdempotent_PayloadMismatch() throws MoneyTransferException {
        var initialBalance = sourceAccount.getBalance();
        var transactionId = UUID.randomUUID();
        var newTransferDto1 = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), initialBalance);
        var newTransferDto2 = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ZERO);
        transactionManagementService.processRequest(transactionId, newTransferDto1, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        var exception = assertThrows(RequestConflictException.class, () -> transactionManagementService.processRequest(transactionId, newTransferDto2, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        assertTrue(exception.getMessage().contains("The JSON body does not match"));
        assertEquals(exception.getHttpStatus(), HttpStatus.CONFLICT);
    }
}
