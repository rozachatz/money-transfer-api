package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling money transfer requests
 * and retrieval of account/transaction resources.
 */
public interface MoneyTransferAPIController {
    @Operation(summary = "Gets transaction by id.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transaction with the given id was found!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "No transaction with the given id was found!",
                            content = @Content)
            })
    ResponseEntity<GetTransferDto> getTransactionById(@Parameter(description = "The transaction id.", required = true) UUID transactionId) throws ResourceNotFoundException;

    @Operation(summary = "Gets account by id.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "An account with the given id was found!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "No account was found!",
                            content = @Content)
            })
    ResponseEntity<GetAccountDto> getAccountById(@Parameter(description = "The account id.", required = true) UUID accountId) throws ResourceNotFoundException;

    @Operation(summary = "Performs an idempotent transfer request. The currency of the transaction is identical to the source's.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Transfer request completed successfully.",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "The specified source or target account was not found. In this system, not found accounts are represented by a UUID with all zeros.",
                            content = @Content),
                    @ApiResponse(responseCode = "402", description = "Insufficient balance for executing the money transfer.",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Transfers within the same account are not allowed.",
                            content = @Content),
                    @ApiResponse(responseCode = "409", description = "This transfer request has already been completed but there is a conflict with the provided json body.",
                            content = @Content)

            })
    ResponseEntity<GetTransferDto> transferRequest(@Parameter(description = "Unique identifier for the transfer request.", required = true) UUID id, @Parameter(description = "The source, target accounts and the amount to be transferred.", required = true) NewTransferDto newTransferDTO, @Parameter(description = "Enforces serializable isolation or pessimistic/optimistic locking, depending on its value.", required = true) ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException;

    @Operation(summary = "Gets all transactions with amount in the given range.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transactions were found!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "No transactions were found!",
                            content = @Content)
            })
    ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(@Parameter(description = "The minimum amount requested for transfer.", required = true) BigDecimal minAmount, @Parameter(description = "The maximum amount requested for transfer.", required = true) BigDecimal maxAmount) throws ResourceNotFoundException;

    @Operation(summary = "Fetches all accounts, with a limitation to the number of results.")
    ResponseEntity<List<GetAccountDto>> getAccountsWithLimit(@Parameter(description = "The maximum number of accounts that will be fetched.", required = true) int limit);

    @Operation(summary = "Fetches all transactions, with a limitation to the number of results.")
    ResponseEntity<List<GetTransferDto>> getTransactionsWithLimit(@Parameter(description = "The maximum number of transactions that will be fetched.", required = true) @PathVariable int limit);

}