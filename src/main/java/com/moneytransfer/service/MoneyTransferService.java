package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.util.UUID;

/**
 * Service that performs the money transfer and persists the associated Transaction.
 */
public interface MoneyTransferService {
    Transaction transfer(UUID requestId, NewTransferDto transferDto, ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException;
}
