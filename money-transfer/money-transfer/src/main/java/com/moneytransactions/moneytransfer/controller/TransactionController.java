package com.moneytransactions.moneytransfer.controller;
import com.moneytransactions.moneytransfer.dto.TransferRequest;
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
  public TransactionController(TransactionService transactionService) { //default: singleton scope
      this.transactionService = transactionService;
    }

    @PostMapping("/transferMoney") //endpoint
    public ResponseEntity<String> transferMoney(@RequestBody TransferRequest transferRequest) {

        /* Call moneyTransfer service method, handle exceptions and response to client. */
        /* TransferRequest: container */

        try {
            transactionService.moneyTransfer(
                    transferRequest.sourceAccountId(),
                    transferRequest.targetAccountId(),
                    transferRequest.amount()
            );

            return ResponseEntity.ok("Successful transfer of funds! :)");
        }
        catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest().body("Error: Insufficient balance in the source account!");
        }
        catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Source or target account not found!");
        }
        catch (SameAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Transactions in the same account are not allowed!");
        }
    }

}

//curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00"}" "http://localhost:8080/transferMoney"