package com.moneytransfer.service;

import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.repository.TransactionRequestRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TransactionRequestServiceImplTest {

    UUID sourceAccountId = UUID.fromString("6a7d71f0-6f12-45a6-91a1-198272a09fe8"), targetAccountId = UUID.fromString("e4c6f84c-8f92-4f2b-90bb-4352e9379bca");
    @Autowired
    private TransactionRequestServiceImpl transactionRequestServiceImpl;
    @Autowired
    private TransactionRequestRepository transactionRequestRepository;

    @Test(expected = InsufficientBalanceException.class)
    public void testRequestFailed() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID transactionRequestId = UUID.randomUUID();
        transactionRequestServiceImpl.processRequest(sourceAccountId, targetAccountId, amount, transactionRequestId);
        Optional<TransactionRequest> retrievedTransactionRequest = transactionRequestRepository.findById(transactionRequestId);
        Assertions.assertTrue(retrievedTransactionRequest.isPresent());
        TransactionRequest transactionRequest = retrievedTransactionRequest.get();
        Assertions.assertEquals(transactionRequest.getRequestStatus(), RequestStatus.FAILED);
        Assertions.assertNull(transactionRequest.getTransaction());
    }

    @Test
    public void testRequestSuccess() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.valueOf(1);
        UUID transactionRequestId = UUID.randomUUID();
        transactionRequestServiceImpl.processRequest(sourceAccountId, targetAccountId, amount, transactionRequestId);
        Optional<TransactionRequest> retrievedTransactionRequest = transactionRequestRepository.findById(transactionRequestId);
        Assertions.assertTrue(retrievedTransactionRequest.isPresent());
        TransactionRequest transactionRequest = retrievedTransactionRequest.get();
        Assertions.assertEquals(transactionRequest.getRequestStatus(), RequestStatus.SUCCESS);
        Assertions.assertNotNull(transactionRequest.getTransaction());
    }

}
