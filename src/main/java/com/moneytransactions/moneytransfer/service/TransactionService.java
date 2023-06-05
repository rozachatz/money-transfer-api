package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.dto.AccountsDTO;
import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;

public interface TransactionService {
    TransferDTO transferFunds(Long sourceAccountId, Long targetAccountId, BigDecimal amount, String mode) throws MoneyTransferException;

    void validateTransfer(AccountsDTO accountsDTO, Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException;

    AccountsDTO getAccountsByIds(Long sourceAccountId, Long targetAccountId, String mode) throws AccountNotFoundException;

}
