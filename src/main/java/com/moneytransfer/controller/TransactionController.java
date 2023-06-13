package com.moneytransfer.controller;

import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.TransferRequestDto;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.TransactionNotFoundException;
import org.springframework.http.ResponseEntity;

public interface TransactionController {
    ResponseEntity<GetTransferDto> transferOptimistic(TransferRequestDto transferRequestDTO) throws MoneyTransferException;

    ResponseEntity<GetTransferDto> transferPessimistic(TransferRequestDto transferRequestDTO) throws MoneyTransferException;

    public ResponseEntity<GetTransferDto> getTransactionById(String transactionId) throws TransactionNotFoundException;
}
