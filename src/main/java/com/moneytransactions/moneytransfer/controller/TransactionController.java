package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.dto.TransferRequestDTO;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.http.ResponseEntity;

public interface TransactionController {
    ResponseEntity<TransferDTO> createOptimisticTransfer(TransferRequestDTO transferRequestDTO) throws MoneyTransferException;
    ResponseEntity<TransferDTO> createPessimisticTransfer(TransferRequestDTO transferRequestDTO) throws MoneyTransferException;
}
