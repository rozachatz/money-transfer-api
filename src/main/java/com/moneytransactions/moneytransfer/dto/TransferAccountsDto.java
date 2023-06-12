package com.moneytransactions.moneytransfer.dto;

import com.moneytransactions.moneytransfer.entity.Account;

public interface TransferAccountsDto {
    Account getSourceAccount();
    Account getTargetAccount();
}
