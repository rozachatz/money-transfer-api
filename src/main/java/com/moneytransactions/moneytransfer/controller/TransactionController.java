package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.dto.TransferRequestDTO;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.http.ResponseEntity;

public interface TransactionController {
    ResponseEntity<TransferDTO> createOptimisticTransaction(TransferRequestDTO transferRequestDTO) throws MoneyTransferException;

    ResponseEntity<TransferDTO> createPessimisticTransaction(TransferRequestDTO transferRequestDTO) throws MoneyTransferException;


}
