package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Type;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.service.AccountManagementService;
import com.moneytransfer.service.TransactionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements the {@link MoneyTransferAPIController}
 */
@RestController
@RequiredArgsConstructor
public class MoneyTransferAPIControllerImpl implements MoneyTransferAPIController {
    private final AccountManagementService accountManagementService;
    private final TransactionManagementService transactionManagementService;


    @PostMapping("/transaction/request/{requestId}/{type}")
    public ResponseEntity<GetTransferDto> transferRequest(@RequestBody NewTransferDto newTransferDto, @PathVariable UUID requestId, @PathVariable Type type) throws MoneyTransferException {
        Transaction transaction = transactionManagementService.processRequest(
                newTransferDto,
                requestId,
                type);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCurrency()));
    }

    @Cacheable
    @GetMapping("/transactions/{minAmount}/{maxAmount}")
    public ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(
            @PathVariable BigDecimal minAmount,
            @PathVariable BigDecimal maxAmount) throws ResourceNotFoundException {
        List<Transaction> transactions = transactionManagementService.getTransactionByAmountBetween(minAmount, maxAmount);
        return ResponseEntity.ok(
                transactions.stream()
                        .map(transaction -> new GetTransferDto(
                                transaction.getId(),
                                transaction.getSourceAccount().getId(),
                                transaction.getTargetAccount().getId(),
                                transaction.getAmount(),
                                transaction.getStatus(),
                                transaction.getCurrency()))
                        .collect(Collectors.toList())
        );
    }

    @Cacheable
    @GetMapping("/accounts/{limit}")
    public ResponseEntity<List<GetAccountDto>> getAccountsWithLimit(@PathVariable int limit) {
        Page<Account> accounts = accountManagementService.getAccountsWithLimit(limit);
        return ResponseEntity.ok(accounts.get().map(account -> new GetAccountDto(account.getId(), account.getBalance(), account.getCurrency())).collect(Collectors.toList()));
    }

    @Cacheable
    @GetMapping("/transactions/{limit}")
    public ResponseEntity<List<GetTransferDto>> getTransactionsWithLimit(@PathVariable int limit) {
        Page<Transaction> transactions = transactionManagementService.getTransactionsWithLimit(limit);
        return ResponseEntity.ok(transactions.get().map(transaction -> new GetTransferDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCurrency())).collect(Collectors.toList()));
    }


    public ResponseEntity<GetAccountDto> getAccountById(@PathVariable UUID id) throws ResourceNotFoundException {
        Account account = accountManagementService.getAccountById(id);
        return ResponseEntity.ok(new GetAccountDto(
                account.getId(),
                account.getBalance(),
                account.getCurrency()));
    }

    @GetMapping("/transaction/{id}")
    public ResponseEntity<GetTransferDto> getTransactionById(@PathVariable UUID id) throws ResourceNotFoundException {
        Transaction transaction = transactionManagementService.getTransactionById(id);
        return ResponseEntity.ok(new GetTransferDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCurrency()));
    }
}
