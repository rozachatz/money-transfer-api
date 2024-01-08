package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling money transfer operations/requests
 * and resource retrieval.
 */
public interface TransactionController {
    ResponseEntity<GetTransferDto> getTransactionById(UUID transactionId) throws ResourceNotFoundException;

    ResponseEntity<GetAccountDto> getAccountById(UUID accountId) throws ResourceNotFoundException;

    ResponseEntity<GetTransferDto> transferOptimistic(NewTransferDto newTransferDTO) throws MoneyTransferException;

    ResponseEntity<GetTransferDto> transferPessimistic(NewTransferDto newTransferDTO) throws MoneyTransferException;

    ResponseEntity<GetTransferDto> transferIdempotentRequest(NewTransferDto newTransferDTO, UUID requestId) throws MoneyTransferException;

    ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException;

    ResponseEntity<List<GetAccountDto>> getAccountsWithLimit(int limit);

}