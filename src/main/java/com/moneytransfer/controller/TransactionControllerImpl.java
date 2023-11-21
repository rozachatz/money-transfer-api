package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.service.TransactionRequestService;
import com.moneytransfer.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * Implements the {@link TransactionController}
 */
@RestController
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {
    private final TransactionService transactionService;
    private final TransactionRequestService transactionRequestService;

    /**
     * Get all transactions with amount withing range
     *
     * @param minAmount
     * @param maxAmount
     * @return A list of GetTransferDto Objects
     * @throws ResourceNotFoundException
     */
    @Cacheable
    @GetMapping("/transactions/{minAmount}/{maxAmount}")
    @Operation(summary = "Get all successful transactions with transferred amount in the given range")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transactions with amount in the given range were found! :)",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Transactions with amount in the given range were NOT found!",
                            content = @Content)
            })
    public ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(
            @Parameter(description = "The minimum transaction amount.", required = true) @PathVariable BigDecimal minAmount,
            @Parameter(description = "The maximum transaction account (included).", required = true) @PathVariable BigDecimal maxAmount) throws ResourceNotFoundException {
        List<Transaction> transactions = transactionService.getTransactionByAmountBetween(minAmount, maxAmount);
        return ResponseEntity.ok(
                transactions.stream()
                        .map(transaction -> new GetTransferDto(
                                transaction.getId(),
                                transaction.getSourceAccount().getId(),
                                transaction.getTargetAccount().getId(),
                                transaction.getAmount()))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Return all accounts with #results<limit
     *
     * @param limit
     * @return List of Account Objects
     */
    @Cacheable
    @GetMapping("/accounts/{limit}")
    @Operation(summary = "Get all accounts. Number of results does not exceed the value of the limit variable.")
    public ResponseEntity<List<GetAccountDto>> getAccountsWithLimit(@Parameter(description = "The maximum number of accounts retrieved.", required = true) @PathVariable int limit) {
        Page<Account> accounts = transactionService.getAccountsWithLimit(limit);
        return ResponseEntity.ok(accounts.get().map(account -> new GetAccountDto(account.getId(), account.getBalance(), account.getCurrency())).collect(Collectors.toList()));
    }

    /**
     * Get Account by id
     *
     * @param id
     * @return associated Account
     * @throws ResourceNotFoundException
     */
    @GetMapping("/account/{id}")
    @Operation(summary = "Get account by id.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Account with the given id was found!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Account with the given id was NOT found!",
                            content = @Content)
            })
    public ResponseEntity<GetAccountDto> getAccountById(@Parameter(description = "The account id.", required = true) @PathVariable UUID id) throws ResourceNotFoundException {
        Account account = transactionService.getAccountById(id);
        return ResponseEntity.ok(new GetAccountDto(
                account.getId(),
                account.getBalance(),
                account.getCurrency()));
    }

    /**
     * Get Transaction by id
     *
     * @param id
     * @return GetTransferDto for the associated Transaction
     * @throws ResourceNotFoundException
     */
    @GetMapping("/transfer/{id}")
    @Operation(summary = "Get transaction by id.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transaction with the given id was found!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Transaction with the given id was NOT found!",
                            content = @Content)
            })
    public ResponseEntity<GetTransferDto> getTransactionById(@Parameter(description = "The transaction id.", required = true) @PathVariable UUID id) throws ResourceNotFoundException {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(new GetTransferDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getAmount()));
    }


    /**
     * New transfer request, optimistic locking
     *
     * @param newTransferDTO
     * @return GetTransferDto for the new Transaction
     * @throws MoneyTransferException
     */
    @Operation(summary = "Initiate a new transaction with optimistic locking.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Transaction was successfully completed!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Source/target account was not found!",
                            content = @Content),
                    @ApiResponse(responseCode = "402", description = "Insufficient balance for executing the transaction.",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Transactions within the same account are not allowed.",
                            content = @Content)
            })
    @PostMapping("/transfer/optimistic")
    public ResponseEntity<GetTransferDto> transferOptimistic(@RequestBody NewTransferDto newTransferDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferOptimistic(
                newTransferDTO.sourceAccountId(),
                newTransferDTO.targetAccountId(),
                newTransferDTO.amount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }

    /**
     * New transfer request, pessimistic locking
     *
     * @param newTransferDTO
     * @return GetTransferDto for the new Transaction
     * @throws MoneyTransferException
     */
    @Operation(summary = "Initiate a new transaction with pessimistic locking.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Transaction was successfully completed!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Source/target account was not found!",
                            content = @Content),
                    @ApiResponse(responseCode = "402", description = "Insufficient balance for executing the transaction.",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Transactions within the same account are not allowed.",
                            content = @Content)
            })
    @PostMapping("/transfer/pessimistic")
    public ResponseEntity<GetTransferDto> transferPessimistic(@RequestBody NewTransferDto newTransferDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferPessimistic(
                newTransferDTO.sourceAccountId(),
                newTransferDTO.targetAccountId(),
                newTransferDTO.amount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }


    /**
     * New IDEMPOTENT tranfer request
     *
     * @param newTransferDto
     * @param requestId
     * @return
     * @throws MoneyTransferException
     */
    @Operation(summary = "Idempotent POST request for a Transaction, given the transactionRequestId.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "The (successful) Transaction associated with this request.",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Source/target account was not found!",
                            content = @Content),
                    @ApiResponse(responseCode = "402", description = "Insufficient balance for executing the transaction.",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Transactions within the same account are not allowed.",
                            content = @Content),
                    @ApiResponse(responseCode = "409", description = "A TransactionRequest conflict is detected (i.e., this is not the first time this request is performed). This means that the transaction request has status failed and/or the json body provided does not match the original.",
                            content = @Content)

            })
    @PostMapping("/transfer/{requestId}")
    public ResponseEntity<GetTransferDto> transferRequestSerializable(@RequestBody NewTransferDto newTransferDto, @PathVariable UUID requestId) throws MoneyTransferException {
        Transaction transaction = transactionRequestService.processRequest(
                newTransferDto.sourceAccountId(),
                newTransferDto.targetAccountId(),
                newTransferDto.amount(),
                requestId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }


}