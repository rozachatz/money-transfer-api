package com.moneytransactions.moneytransfer.service;
import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import jakarta.transaction.Transactional;
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

        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(()->new AccountNotFoundException("Source account not found."));  /* AC4: account not exists */

        Account targetAccount = accountRepository.findById(targetAccountId)
                .orElseThrow(()->new AccountNotFoundException("Target account not found.")); /* AC4: account not exists */

        //New transaction object
        Transaction transaction = new Transaction(sourceAccount, targetAccount, amount, "EUR");

        if (!sourceAccountId.equals(targetAccountId)) {

            if (sourceAccount.getBalance().compareTo(amount) < 0) {   /* AC2: Insufficient Balance */

                throw new InsufficientBalanceException("Insufficient balance in the source account");

            }
            else { /* AC1: happy path */

                //Credit target & save
                targetAccount.credit(amount);
                accountRepository.save(targetAccount);

                //Debit src & save
                sourceAccount.debit(amount);
                accountRepository.save(sourceAccount);

                /*Save transaction */
                transactionRepository.save(transaction);
                System.out.println("Happy path: transaction is completed!");

            }
        }
        else { /* AC3: transfer same account */

            throw new SameAccountException("Transfer between the same account is not allowed");

        }


    }

    }
