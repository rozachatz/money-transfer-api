package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.util.UUID;

/**
 * Service that performs the money transfer and persists the associated Transaction.
 */
interface MoneyTransferService {
    Transaction transferSerializable(UUID id, NewTransferDto transferDto) throws MoneyTransferException;

    Transaction transferOptimistic(UUID id, NewTransferDto transferDto) throws MoneyTransferException;

    Transaction transferPessimistic(UUID id, NewTransferDto transferDto) throws MoneyTransferException;
}
