package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.GetTransferDto;
import com.moneytransactions.moneytransfer.dto.TransferRequestDto;
import com.moneytransactions.moneytransfer.entity.Transaction;
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
    public ResponseEntity<GetTransferDto> createOptimisticTransfer(@RequestBody TransferRequestDto transferRequestDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferFundsOptimistic(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }

    @PostMapping("/transfer/pessimistic")
    public ResponseEntity<GetTransferDto> createPessimisticTransfer(@RequestBody TransferRequestDto transferRequestDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferFundsPessimistic(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }

}