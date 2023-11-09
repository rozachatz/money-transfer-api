package com.moneytransfer.service;

import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.TransactionRequestRepository;
import org.junit.Before;
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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TransactionRequestServiceImplTest {

    @Autowired
    private TransactionRequestServiceImpl transactionRequestServiceImpl;

    @Autowired
    private TransactionRequestRepository transactionRequestRepository;
    UUID id1 = UUID.fromString("6a7d71f0-6f12-45a6-91a1-198272a09fe8"), id2=UUID.fromString("e4c6f84c-8f92-4f2b-90bb-4352e9379bca");
    @Test(expected = InsufficientBalanceException.class)
    public void testExecute_Fail() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.valueOf(1000000000);
        UUID transactionRequestId = UUID.randomUUID();
        transactionRequestServiceImpl.processRequest(id1,id2,amount,transactionRequestId );
        Optional< TransactionRequest> retrievedTransactionRequest = transactionRequestRepository.findById(transactionRequestId);
        assertNotNull(retrievedTransactionRequest);
        Assertions.assertTrue(retrievedTransactionRequest.isPresent());
        TransactionRequest transactionRequest = retrievedTransactionRequest.get();
        Assertions.assertEquals(transactionRequestId, transactionRequest.getRequestId());
        Assertions.assertNull(transactionRequest.getTransaction());
        Assertions.assertNotNull(transactionRequest.getErrorMessage());
    }

    @Test
    public void testExecute_Success() throws MoneyTransferException {
        BigDecimal amount = BigDecimal.valueOf(1);
        UUID transactionRequestId = UUID.randomUUID();
        transactionRequestServiceImpl.processRequest(id1, id2, amount, transactionRequestId );
        Optional< TransactionRequest> retrievedTransactionRequest = transactionRequestRepository.findById(transactionRequestId);
        assertNotNull(retrievedTransactionRequest);
        Assertions.assertTrue(retrievedTransactionRequest.isPresent());
        TransactionRequest transactionRequest = retrievedTransactionRequest.get();
        Assertions.assertEquals(transactionRequestId, transactionRequest.getRequestId());
        Assertions.assertNotNull(transactionRequest.getTransaction());
        Assertions.assertNull(transactionRequest.getErrorMessage());
    }

}
