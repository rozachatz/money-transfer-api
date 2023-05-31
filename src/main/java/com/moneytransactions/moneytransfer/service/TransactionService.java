package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService implements TransactionServiceInterface { //responsible for business logic, error handling

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional // ACID
    public void moneyTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {
        List<Account> accounts = validateTransfer(sourceAccountId, targetAccountId, amount);

        /* AC1: Happy path */
        Account sourceAccount = accounts.get(0);
        Account targetAccount = accounts.get(1);

        Transaction transaction = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, "EUR");

        targetAccount.credit(amount);
        sourceAccount.debit(amount);

        accountRepository.saveAll(accounts);
        transactionRepository.save(transaction);

    }

    public List<Account> validateTransfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {
        List<Account> accounts = accountRepository.findAllByIdAndLock(Arrays.asList(sourceAccountId, targetAccountId));

        Account sourceAccount = accounts.stream()
                .filter(account -> account.getId().equals(sourceAccountId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Source account not found."));

        Account targetAccount = accounts.stream()
                .filter(account -> account.getId().equals(targetAccountId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Target account not found."));

        if (sourceAccount.getBalance().compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            throw new InsufficientBalanceException("Insufficient balance in the source account.");
        }
        if (sourceAccount.getId().equals(targetAccount.getId())) {  /* AC3: Same Account */
            throw new SameAccountException("Transactions in the same account are not allowed.");
        }
        return List.of(sourceAccount, targetAccount);

    }

}