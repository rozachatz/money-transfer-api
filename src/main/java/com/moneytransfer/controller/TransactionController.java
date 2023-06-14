package com.moneytransfer.controller;

import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.TransferRequestDto;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface TransactionController {
    ResponseEntity<GetTransferDto> transferOptimistic(TransferRequestDto transferRequestDTO) throws MoneyTransferException;
    ResponseEntity<GetTransferDto> transferPessimistic(TransferRequestDto transferRequestDTO) throws MoneyTransferException;
    ResponseEntity<GetTransferDto> getById(UUID transactionId) throws ResourceNotFoundException;
    ResponseEntity<GetTransferDto> transfer(TransferRequestDto transferRequestDTO) throws MoneyTransferException;
}