package com.moneytransfer.dto;

import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;

/**
 * A Dto representing the {@link Account} entities participating in a {@link Transaction}
 */
public interface TransferAccountsDto {
    Account getSourceAccount();

    Account getTargetAccount();
}
