package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.GetTransferDto;
import com.moneytransactions.moneytransfer.dto.TransferRequestDto;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.TransactionNotFoundException;
import com.moneytransactions.moneytransfer.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RequestMapping("/api")
@RestController
public class TransactionControllerImpl implements TransactionController {
    private final TransactionService transactionService;
    public TransactionControllerImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping("/transfer/{id}")
    public ResponseEntity<GetTransferDto> getTransactionById(@PathVariable String id) throws TransactionNotFoundException {
        Transaction transaction = transactionService.getTransactionById(UUID.fromString(id));
        return ResponseEntity.ok(new GetTransferDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getAmount()));
    }
    @PostMapping("/transfer/optimistic")
    public ResponseEntity<GetTransferDto> createOptimisticTransfer(@RequestBody TransferRequestDto transferRequestDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferOptimistic(
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
        Transaction transaction = transactionService.transferPessimistic(
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