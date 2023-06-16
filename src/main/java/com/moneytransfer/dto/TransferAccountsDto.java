package com.moneytransfer.dto;

import com.moneytransfer.entity.Account;

public interface TransferAccountsDto {
    Account getSourceAccount();

    Account getTargetAccount();
}
