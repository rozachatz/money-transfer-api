package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferRequest;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.service.TransactionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionControllerImpl implements TransactionController {
    private final TransactionServiceImpl transactionServiceImpl;

    public TransactionControllerImpl(TransactionServiceImpl transactionServiceImpl) { //default: singleton scope
        this.transactionServiceImpl = transactionServiceImpl;
    }

    @PostMapping("/transferMoney") //endpoint
    public ResponseEntity<String> transferMoney(@RequestBody TransferRequest transferRequest) throws MoneyTransferException {
        /* Call moneyTransfer service method, handle exceptions and response to client. */
        /* TransferRequest: container */
        transactionServiceImpl.moneyTransfer(
                transferRequest.sourceAccountId(),
                transferRequest.targetAccountId(),
                transferRequest.amount()
        );
        return ResponseEntity.ok("Money transfer was successful");
    }
}


//curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00"}" "http://localhost:8080/transferMoney"