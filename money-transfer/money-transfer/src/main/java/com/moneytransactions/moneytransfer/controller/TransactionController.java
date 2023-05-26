package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController

public class TransactionController {
    private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @PostMapping("/transferMoney")
    public ResponseEntity<String> transferMoney(@RequestBody Transaction transaction) {
        // Call the moneyTransfer method of the TransactionService and handle any exceptions
        try {
            transactionService.moneyTransfer(
                    transaction.getSourceAccountId(),
                    transaction.getTargetAccountId(),
                    transaction.getAmount()
            );
            return ResponseEntity.ok("Money transfer successful");
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest().body("Insufficient balance in the source account");
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Source or target account not found");
        } catch (SameAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transactions in the same account are not allowed.");
        }
    }

}

//curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00" }" "http://localhost:8080/transferMoney"