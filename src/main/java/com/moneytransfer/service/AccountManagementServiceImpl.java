package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of {@link AccountManagementService}
 */
@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {
    /**
     * The Account repository.
     */
    private final AccountRepository accountRepository;

    /**
     * Get Account by accountId.
     *
     * @param accountId
     * @return Transaction
     * @throws ResourceNotFoundException
     */
    public Account getAccountById(final UUID accountId) throws ResourceNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    var errorMessage = "Account with ID: " + accountId + " was not found.";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Gets the source/target account pair by their ids.
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIds(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    String errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Gets the account by id or returns the account assigned as default.
     *
     * @param accountId
     * @return
     * @throws ResourceNotFoundException
     */
    public Account getAccountByIdOrReturnDefault(UUID accountId) throws ResourceNotFoundException {
        String default_UUID = "00000000-0000-0000-0000-000000000000";
        return accountRepository.findById(accountId).orElse(getAccountById(UUID.fromString(default_UUID)));
    }

    /**
     * Gets all accounts with limited number of results.
     *
     * @param limit
     * @return Accounts
     */
    public Page<Account> getAccountsWithLimit(final int limit) {
        var pageRequest = PageRequest.of(0, limit);
        return accountRepository.findAll(pageRequest);
    }

    /**
     * Gets the {@link TransferAccountsDto} with optimistic locking
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */

    public TransferAccountsDto getAccountsByIdsOptimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    var errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    /**
     * Gets the {@link TransferAccountsDto} with pessimistic locking
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIdsPessimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> {
                    var errorMessage = "Source/target account not found. Source Account ID: " + sourceAccountId + ", Target Account ID: " + targetAccountId + ".";
                    return new ResourceNotFoundException(errorMessage);
                });
    }
}
