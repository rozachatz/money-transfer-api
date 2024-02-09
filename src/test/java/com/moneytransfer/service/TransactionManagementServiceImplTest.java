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
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
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
    public void testFailedRequest() {
        BigDecimal amount = sourceAccount.getBalance();
        UUID nonExistentAccountId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> transactionManagementService.processRequest(new NewTransferDto(sourceAccount.getId(), nonExistentAccountId, amount), requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        checkTransactionPersistedAndStatus(requestId, RequestStatus.FAILED);
    }

    /**
     * Validates that a successful transfer request is persisted.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testSuccessfulRequest() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance();
        UUID requestId = UUID.randomUUID();
        transactionManagementService.processRequest(new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount), requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        checkTransactionPersistedAndStatus(requestId, RequestStatus.SUCCESS);
    }

    /**
     * Checks the idempotent behavior of a transfer request.
     * For a successful {@link Transaction}, objects returned should be identical.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testSuccessfulRequest_IdempotentBehavior() throws MoneyTransferException {
        UUID requestId = UUID.randomUUID();
        BigDecimal amount = sourceAccount.getBalance();
        NewTransferDto newTransferDto = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount);
        Transaction transaction1 = transactionManagementService.processRequest(newTransferDto, requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        Transaction transaction2 = transactionManagementService.processRequest(newTransferDto, requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    /**
     * Checks the idempotent behavior of a failed transfer request.
     * For a successful {@link Transaction}, objects returned should be identical.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testFailedRequest_IdempotentBehavior() {
        UUID requestId = UUID.randomUUID();
        BigDecimal amount = sourceAccount.getBalance().multiply(BigDecimal.TEN);
        NewTransferDto newTransferDto = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount);
        assertThrows(InsufficientBalanceException.class, () -> transactionManagementService.processRequest(newTransferDto, requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        assertThrows(RequestConflictException.class, () -> transactionManagementService.processRequest(newTransferDto, requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
    }

    /**
     * Checks the idempotent behavior of a successful transfer request.
     * The payload of the request should remain the same.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testRequests_WrongPayload() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance();
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto1 = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), amount);
        NewTransferDto newTransferDto2 = new NewTransferDto(sourceAccount.getId(), targetAccount.getId(), BigDecimal.ZERO);
        transactionManagementService.processRequest(newTransferDto1, requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        assertThrows(RequestConflictException.class, () -> transactionManagementService.processRequest(newTransferDto2, requestId, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
    }


    /**
     * Checks that the {@link Transaction} with the given id is persisted
     * and has the given status.
     *
     * @param requestId
     * @param status
     */
    private void checkTransactionPersistedAndStatus(UUID requestId, RequestStatus status) {
        Optional<Transaction> retrievedTransaction = transactionRepository.findById(requestId);
        assertTrue(retrievedTransaction.isPresent());
        assertEquals(retrievedTransaction.get().getStatus(), status);
    }

}
