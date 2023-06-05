package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.dto.TransferRequestDTO;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.service.TransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnExpression("${myapp.controller-with-interfaces.enabled:true}")
public class TransactionControllerImplWithInterface implements TransactionController {
    private final TransactionService transactionService;

    public TransactionControllerImplWithInterface(TransactionService transactionService) {
        System.out.println("Interface implementation!");
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions/optimistic") //Endpoint
    public ResponseEntity<TransferDTO> createOptimisticTransaction(@RequestBody TransferRequestDTO transferRequestDTO) throws MoneyTransferException {
        return ResponseEntity.ok(transactionService.transferFunds(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount(),
                "Optimistic"
        ));
    }

    @PostMapping("/transactions/pessimistic") //Endpoint
    public ResponseEntity<TransferDTO> createPessimisticTransaction(@RequestBody TransferRequestDTO transferRequestDTO) throws MoneyTransferException {
        return ResponseEntity.ok(transactionService.transferFunds(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount(),
                "Pessimistic"
        ));
    }

}


//curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00"}" "http://localhost:8080/transferMoney"