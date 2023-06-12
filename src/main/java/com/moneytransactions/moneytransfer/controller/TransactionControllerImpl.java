package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.GetTransferDto;
import com.moneytransactions.moneytransfer.dto.TransferRequestDto;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.service.TransactionServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnExpression("!${myapp.controller.enabled:true}")
public class TransactionControllerImpl implements TransactionController {
    private final TransactionServiceImpl transactionServiceImpl;

    public TransactionControllerImpl(TransactionServiceImpl transactionServiceImpl) {
        this.transactionServiceImpl = transactionServiceImpl;
    }

    @PostMapping("/transfer/optimistic")
    public ResponseEntity<GetTransferDto> createOptimisticTransfer(@RequestBody TransferRequestDto transferRequestDTO) throws MoneyTransferException {
        Transaction transaction = transactionServiceImpl.transferFundsOptimistic(
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
        Transaction transaction = transactionServiceImpl.transferFundsPessimistic(
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
