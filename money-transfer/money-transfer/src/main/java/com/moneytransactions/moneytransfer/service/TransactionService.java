package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import java.math.BigDecimal;

@Service
@Transactional
public class TransactionService {
    //responsible for business logic, error handling
    private final AccountRepository targetAccountRepository;
    private final AccountRepository sourceAccountRepository;
    private final TransactionRepository transactionRepository;
    @Autowired
    public TransactionService(AccountRepository targetAccountRepository, AccountRepository sourceAccountRepository,TransactionRepository transactionRepository){
            this.targetAccountRepository = targetAccountRepository;
            this.sourceAccountRepository = sourceAccountRepository;
            this.transactionRepository = transactionRepository;
    }

    public void moneyTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        // AC4 and Given in AC1-3
        Account sourceAccount = sourceAccountRepository.findById(sourceAccountId)
                .orElseThrow(()->new AccountNotFoundException("Source account not found."));

        Account targetAccount = targetAccountRepository.findById(targetAccountId)
                .orElseThrow(()->new AccountNotFoundException("Target account not found."));

        Transaction transaction = new Transaction(sourceAccountId, targetAccountId, amount, "EUR");

        if (!sourceAccount.equals(targetAccount)) {
            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                // AC2
                throw new InsufficientBalanceException("Insufficient balance in the source account");
            }
            else {
                // AC1: Happy Transfer!
                // Update target account by crediting the amount
                targetAccount.credit(amount);
                targetAccountRepository.save(targetAccount);

                // Update source account by debiting the amount
                sourceAccount.debit(amount);
                sourceAccountRepository.save(sourceAccount);

                // save the transaction only if successful
                transactionRepository.save(transaction);

                System.out.println("Happy path: transaction is completed!");
            }
        } else{
            // AC3
            throw new SameAccountException("Transfer between the same account is not allowed");
        }


    }

    }
