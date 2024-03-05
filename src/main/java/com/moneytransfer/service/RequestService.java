package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Request;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.util.UUID;

/**
 * Service that submits, resolves and gets Requests.
 */
public interface RequestService {
    Request getRequest(UUID requestId);

    Request submitRequest(UUID requestId, NewTransferDto newTransferDto) throws MoneyTransferException;

    Request resolveRequest(UUID requestId, Transaction transaction) throws MoneyTransferException;
}
