package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service that manages accounts
 */
public interface AccountManagementService {
    Account getAccountById(final UUID accountId) throws ResourceNotFoundException;

    Account getAccountByIdOrReturnDefault(final UUID accountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIds(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException;

    Page<Account> getAccountsWithLimit(final int limit);

    TransferAccountsDto getAccountsByIdsOptimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIdsPessimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException;

}
