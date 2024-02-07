package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.enums.Type;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling money transfer operations/requests
 * and resource retrieval.
 */
public interface MoneyTransferAPIController {
    @Operation(summary = "Gets transaction by id.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transaction with the given id was found!",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "No transaction was found!",
                            content = @Content)
            })
    ResponseEntity<GetTransferDto> getTransactionById(@Parameter(description = "The transaction id.", required = true) UUID transactionId) throws ResourceNotFoundException;

    @GetMapping("/account/{id}")
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

    @Operation(summary = "Performs an idempotent transfer request. The currency is always same as the currency of the source account.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Transaction request completed successfully.",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetTransferDto.class))}),
                    @ApiResponse(responseCode = "404", description = "Source/target account was not found!",
                            content = @Content),
                    @ApiResponse(responseCode = "402", description = "Insufficient balance for executing the transaction.",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Transfers in the same account are not allowed.",
                            content = @Content),
                    @ApiResponse(responseCode = "409", description = "A TransactionRequest conflict is detected (i.e., this is not the first time this request is performed). This means that the transaction request has status failed and/or the json body provided does not match the original.",
                            content = @Content)

            })
    ResponseEntity<GetTransferDto> transferRequest(@Parameter(description = "The accounts and the amount that will be transferred from source to target account.", required = true) NewTransferDto newTransferDTO, @Parameter(description = "Unique identifier for the request.", required = true) UUID requestId, @Parameter(description = "Isolation/locking type of the transaction request.", required = true) Type type) throws MoneyTransferException;

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
    ResponseEntity<List<GetTransferDto>> getTransactionsWithLimit(@Parameter(description = "The maximum number of transactions that will be fetched.", required = true)  @PathVariable int limit);

}