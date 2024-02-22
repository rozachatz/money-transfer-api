package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.service.GetAccountService;
import com.moneytransfer.service.GetTransactionService;
import com.moneytransfer.service.MoneyTransferService;
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
    private final GetAccountService getAccountService;
    private final GetTransactionService getTransactionService;
    private final MoneyTransferService moneyTransferService;


    @PostMapping("/transfer/request/{transactionId}/{concurrencyControlMode}")
    public ResponseEntity<GetTransferDto> transferRequest(@PathVariable UUID transactionId, @RequestBody NewTransferDto newTransferDto, @PathVariable ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        Transaction transaction = moneyTransferService.transfer(
                transactionId,
                newTransferDto,
                concurrencyControlMode);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getTransactionId(),
                        transaction.getSourceAccount().getAccountId(),
                        transaction.getTargetAccount().getAccountId(),
                        transaction.getAmount(),
                        transaction.getTransactionStatus(),
                        transaction.getCurrency()));
    }

    @GetMapping("/transactions/{minAmount}/{maxAmount}")
    public ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(
            @PathVariable BigDecimal minAmount,
            @PathVariable BigDecimal maxAmount) throws ResourceNotFoundException {
        List<Transaction> transactions = getTransactionService.getTransactionByAmountBetween(minAmount, maxAmount);
        return ResponseEntity.ok(
                transactions.stream()
                        .map(transaction -> new GetTransferDto(
                                transaction.getTransactionId(),
                                transaction.getSourceAccount().getAccountId(),
                                transaction.getTargetAccount().getAccountId(),
                                transaction.getAmount(),
                                transaction.getTransactionStatus(),
                                transaction.getCurrency()))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/transactions/{limit}")
    public ResponseEntity<List<GetTransferDto>> getTransactionsWithLimit(@PathVariable int limit) {
        Page<Transaction> transactions = getTransactionService.getTransactionsWithLimit(limit);
        return ResponseEntity.ok(transactions.get().map(transaction -> new GetTransferDto(
                transaction.getTransactionId(),
                transaction.getSourceAccount().getAccountId(),
                transaction.getTargetAccount().getAccountId(),
                transaction.getAmount(),
                transaction.getTransactionStatus(),
                transaction.getCurrency())).collect(Collectors.toList()));
    }

    @GetMapping("/transaction/{id}")
    public ResponseEntity<GetTransferDto> getTransactionById(@PathVariable UUID id) throws ResourceNotFoundException {
        Transaction transaction = getTransactionService.getTransactionById(id);
        return ResponseEntity.ok(new GetTransferDto(
                transaction.getTransactionId(),
                transaction.getSourceAccount().getAccountId(),
                transaction.getTargetAccount().getAccountId(),
                transaction.getAmount(),
                transaction.getTransactionStatus(),
                transaction.getCurrency()));
    }

    @GetMapping("/accounts/{limit}")
    public ResponseEntity<List<GetAccountDto>> getAccountsWithLimit(@PathVariable int limit) {
        Page<Account> accounts = getAccountService.getAccountsWithLimit(limit);
        return ResponseEntity.ok(accounts.get().map(account -> new GetAccountDto(account.getAccountId(), account.getBalance(), account.getCurrency())).collect(Collectors.toList()));
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<GetAccountDto> getAccountById(@PathVariable UUID id) throws ResourceNotFoundException {
        Account account = getAccountService.getAccountById(id);
        return ResponseEntity.ok(new GetAccountDto(
                account.getAccountId(),
                account.getBalance(),
                account.getCurrency()));
    }
}
