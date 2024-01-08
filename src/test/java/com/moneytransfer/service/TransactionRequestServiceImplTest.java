/**
 Test class for {@link com.moneytransfer.service.TransactionRequestService}
 This test uses embedded h2 db.
 */
package com.moneytransfer.service;

import com.beust.ah.A;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.repository.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TransactionRequestServiceImplTest {
    @Autowired
    private TransactionRequestServiceImpl transactionRequestServiceImpl;
    @Autowired
    private TransactionRequestRepository transactionRequestRepository;
    @Autowired
    private AccountRepository accountRepository;
    Account sourceAccount, targetAccount;
    @Before
    public void setup(){
        sourceAccount = new Account(0, UUID.randomUUID(), BigDecimal.valueOf(10), Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), BigDecimal.valueOf(10), Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(targetAccount,sourceAccount));
    }
    @Test(expected = MoneyTransferException.class)
    public void testRequestFailed() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance().multiply(BigDecimal.valueOf(10));
        UUID transactionRequestId = UUID.randomUUID();
        transactionRequestServiceImpl.processRequest(sourceAccount.getId(), targetAccount.getId(), amount, transactionRequestId);
        Optional<TransactionRequest> retrievedTransactionRequest = transactionRequestRepository.findById(transactionRequestId);
        Assertions.assertTrue(retrievedTransactionRequest.isPresent());
        TransactionRequest transactionRequest = retrievedTransactionRequest.get();
        Assertions.assertEquals(transactionRequest.getRequestStatus(), RequestStatus.FAILED);
        Assertions.assertNull(transactionRequest.getTransaction());
    }

    @Test
    public void testRequestSuccess() throws MoneyTransferException {
        BigDecimal amount = sourceAccount.getBalance();
        UUID transactionRequestId = UUID.randomUUID();
        transactionRequestServiceImpl.processRequest(sourceAccount.getId(), targetAccount.getId(), amount, transactionRequestId);
        Optional<TransactionRequest> retrievedTransactionRequest = transactionRequestRepository.findById(transactionRequestId);
        Assertions.assertTrue(retrievedTransactionRequest.isPresent());
        TransactionRequest transactionRequest = retrievedTransactionRequest.get();
        Assertions.assertEquals(transactionRequest.getRequestStatus(), RequestStatus.SUCCESS);
        Assertions.assertNotNull(transactionRequest.getTransaction());
    }

}
