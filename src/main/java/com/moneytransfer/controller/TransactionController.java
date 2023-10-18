package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.TransferRequestDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionController {
    ResponseEntity<GetTransferDto> getTransactionById(UUID transactionId) throws ResourceNotFoundException;

    ResponseEntity<GetAccountDto> getAccountById(UUID accountId) throws ResourceNotFoundException;

    ResponseEntity<GetTransferDto> transferOptimistic(TransferRequestDto transferRequestDTO) throws MoneyTransferException;

    ResponseEntity<GetTransferDto> transferPessimistic(TransferRequestDto transferRequestDTO) throws MoneyTransferException;

    ResponseEntity<GetTransferDto> transfer(TransferRequestDto transferRequestDTO, UUID requestId) throws MoneyTransferException;
    ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(BigDecimal minAmount, BigDecimal maxAmount) throws ResourceNotFoundException;
    ResponseEntity<Page<Account>> getAccountsWithLimit(int limit);

}