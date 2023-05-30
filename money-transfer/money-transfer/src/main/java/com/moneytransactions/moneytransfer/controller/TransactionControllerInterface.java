package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferRequest;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.http.ResponseEntity;

public interface TransactionControllerInterface {
    ResponseEntity<String> transferMoney(TransferRequest transferRequest) throws MoneyTransferException;

}
