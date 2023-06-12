package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.GetTransferDto;
import com.moneytransactions.moneytransfer.dto.TransferRequestDto;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.TransactionNotFoundException;
import org.springframework.http.ResponseEntity;

public interface TransactionController {
    ResponseEntity<GetTransferDto> createOptimisticTransfer(TransferRequestDto transferRequestDTO) throws MoneyTransferException;

    ResponseEntity<GetTransferDto> createPessimisticTransfer(TransferRequestDto transferRequestDTO) throws MoneyTransferException;

    public ResponseEntity<GetTransferDto> getTransactionById(String transactionId) throws TransactionNotFoundException;
}
