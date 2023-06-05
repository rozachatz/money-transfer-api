package com.moneytransactions.moneytransfer.dto;

import com.moneytransactions.moneytransfer.entity.Account;

public interface AccountsDTO {
    Account getSourceAccount();

    Account getTargetAccount();
}
