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
@Transactional // rollback if error occurs, data consistency
public class TransactionService { //responsible for business logic, error handling
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    public TransactionService(AccountRepository accountRepository,TransactionRepository transactionRepository){
            this.accountRepository = accountRepository;
            this.transactionRepository = transactionRepository;
    }

    public void moneyTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        // checking if accounts exist
        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(()->new AccountNotFoundException("Source account not found."));

        Account targetAccount = accountRepository.findById(targetAccountId)
                .orElseThrow(()->new AccountNotFoundException("Target account not found."));

        Transaction transaction = new Transaction(sourceAccount, targetAccount, amount, "EUR");

        if (!sourceAccount.equals(targetAccount)) {
            if (sourceAccount.getBalance().compareTo(amount) < 0) {  // AC2
                throw new InsufficientBalanceException("Insufficient balance in the source account");
            }
            else { //AC1
                // update target account by crediting the amount
                targetAccount.credit(amount);
                accountRepository.save(targetAccount);

                // update source account by debiting the amount
                sourceAccount.debit(amount);
                accountRepository.save(sourceAccount);

                // save only if successful
                transactionRepository.save(transaction);
                System.out.println("Happy path: transaction is completed!");
            }
        } else{ // AC3
            throw new SameAccountException("Transfer between the same account is not allowed");
        }


    }

    }
