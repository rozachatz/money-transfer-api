package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.dto.TransferRequestDTO;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.service.TransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnExpression("${myapp.controller-with-interfaces.enabled:true}")
public class TransactionControllerImplWithInterface implements TransactionController {
    private final TransactionService transactionService;

    public TransactionControllerImplWithInterface(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer/optimistic")
    public ResponseEntity<TransferDTO> createOptimisticTransfer(@RequestBody TransferRequestDTO transferRequestDTO) throws MoneyTransferException {
        TransferDTO createdtransferDTO = transactionService.transferFunds(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount(),
                "Optimistic");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdtransferDTO);
    }

    @PostMapping("/transfer/pessimistic")
    public ResponseEntity<TransferDTO> createPessimisticTransfer(@RequestBody TransferRequestDTO transferRequestDTO) throws MoneyTransferException {
        TransferDTO createdtransferDTO = transactionService.transferFunds(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount(),
                "Pessimistic");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdtransferDTO);
    }

}